package net.bruhitsalex.sjch

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.bruhitsalex.sjch.types.EmbedInformation
import net.bruhitsalex.sjch.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl
import java.time.LocalDateTime
import java.util.concurrent.Executors

const val defaultHandlerId = "default"

open class ICommandHandler(
    private val bot: JDA?,
    commandsBasePackage: String,
    handlerId: String = defaultHandlerId,
    private val commandPrefix: String = "!",
    private val announceUnknownCommand: Boolean = false,
    threadCount: Int = 2,
    private val embedInformation: EmbedInformation = EmbedInformation(
        "Template",
        "2020",
        "https://storage.needpix.com/thumbs/bot-icon-2883144_1280.png"
    )
): ListenerAdapter() {

    val commandRegistrator = CommandRegistrator()
    private val threadPool = Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher()

    init {
        commandRegistrator.registerCommands(handlerId, commandsBasePackage)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || !event.message.contentRaw.startsWith(commandPrefix) || event.member == null) return

        val args = event.message.contentRaw.split(" ")
        val requestedCmdName = args[0].substring(1)
        val commandArgs = args.subList(1, args.size)

        val cmd = commandRegistrator.getCommand(requestedCmdName)
        if (cmd == null) {
            if (announceUnknownCommand) {
                event.message.replyEmbeds(createBadEmbed("Unknown Command", "The command `$requestedCmdName` does not exist!").build()).queue()
            }
            return
        }

        if (commandRegistrator.hasCooldown(event.member!!, cmd)) {
            val cooldownLeft = commandRegistrator.getCooldownLeft(event.member!!, cmd)
            event.message.replyEmbeds(createBadEmbed("Command Cooldown", "You are on cooldown for another ${Utils.formatDuration(cooldownLeft)}!").build()).queue()
            return
        }

        if (!commandRegistrator.hasRequiredRoles(event.member!!, cmd)) {
            event.message.replyEmbeds(createBadEmbed("Missing Roles", "You are missing the required roles to execute this command!").build()).queue()
            return
        }

        if (cmd.channelsRequired != null && !cmd.channelsRequired.contains(event.channel.id)) {
            event.message.replyEmbeds(createBadEmbed("Wrong Channel", "This command cannot be used in this channel.").build()).queue()
            return
        }

        val params = Array<Any>(size = cmd.executeFunction.parameters.size, init = {})
        if (cmd.executeFunction.parameters != null) {
            for ((i, parameter) in cmd.executeFunction.parameters.withIndex()) {
                when (parameter.type) {
                    MessageReceivedEvent::class.java -> params[i] = (event)
                    List::class.java -> params[i] = (commandArgs)
                    Member::class.java -> params[i] = (event.member!!)
                    Guild::class.java -> params[i] = (event.guild)
                    TextChannelImpl::class.java -> params[i] = (event.channel)
                    TextChannel::class.java -> params[i] = (event.channel)
                    Message::class.java -> params[i] = (event.message)
                    JDA::class.java -> params[i] = (bot!!)
                    EmbedBuilder::class.java -> params[i] = (createDefaultEmbed())
                    else -> throw IllegalArgumentException("Unknown parameter type: ${parameter.type}")
                }
            }
        }

        commandRegistrator.setCooldown(event.member!!, cmd)

        runBlocking {
            launch(threadPool) {
                try {
                    cmd.executeFunction.invoke(cmd.instance, *params)
                } catch (e: Exception) {
                    event.message.replyEmbeds(createBadEmbed("Command Error", "An error occurred while executing the command `${cmd.name}`!").build()).queue()
                    e.printStackTrace()
                }
            }
        }
    }

    open fun createDefaultEmbed(): EmbedBuilder {
        return EmbedBuilder()
            .setTitle("Title")
            .setColor(Utils.GREEN)
            .setDescription("Description")
            .setFooter("${embedInformation.name} ${embedInformation.startingYear}-${LocalDateTime.now().year}", embedInformation.iconUrl)
            .setTimestamp(LocalDateTime.now())
    }

    private fun createBadEmbed(title: String, description: String): EmbedBuilder {
        return createDefaultEmbed()
            .setTitle(title)
            .setColor(Utils.RED)
            .setDescription(description)
    }

}