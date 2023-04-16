package net.bruhitsalex.sjch

import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.bruhitsalex.sjch.types.EmbedInformation
import net.bruhitsalex.sjch.utils.Utils
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executors

const val defaultHandlerId = "default"

open class ICommandHandler(
    private val bot: JDA?,
    private val commandsBasePackage: String,
    private val handlerId: String = defaultHandlerId,
    private val commandPrefix: String = "!",
    private val announceUnknownCommand: Boolean = false,
    private val threadCount: Int = 2,
    private val embedInformation: EmbedInformation = EmbedInformation("Template", "2020", "https://storage.needpix.com/thumbs/bot-icon-2883144_1280.png")
): ListenerAdapter() {

    private val commandRegistrator = CommandRegistrator()
    private val threadPool = Executors.newFixedThreadPool(threadCount).asCoroutineDispatcher()

    init {
        commandRegistrator.registerCommands(handlerId, commandsBasePackage)
    }

    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot || !event.message.contentRaw.startsWith("!") || event.member == null) return

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

        val params = mutableListOf<Any>()
        if (cmd.executeFunction.parameters != null) {
            for ((i, parameter) in cmd.executeFunction.parameters.withIndex()) {
                when (parameter.type) {
                    MessageReceivedEvent::class.java -> params.add(event)
                    List::class.java -> params.add(commandArgs)
                    Member::class.java -> params.add(event.member!!)
                    TextChannelImpl::class.java -> params.add(event.channel)
                    JDA::class.java -> params.add(bot!!)
                    EmbedBuilder::class.java -> params.add(createDefaultEmbed())
                    else -> throw IllegalArgumentException("Unknown parameter type: ${parameter.type}")
                }
            }
        }

        commandRegistrator.setCooldown(event.member!!, cmd)

        runBlocking {
            launch(threadPool) {
                try {
                    println(params[0].javaClass.name)
                    println(Arrays.toString(cmd.executeFunction.parameters))
                    cmd.executeFunction.invoke(null, params.toTypedArray())
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