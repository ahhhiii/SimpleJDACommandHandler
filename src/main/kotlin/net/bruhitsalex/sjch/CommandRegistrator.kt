package net.bruhitsalex.sjch

import io.github.classgraph.ClassGraph
import net.bruhitsalex.sjch.types.AcceptedParams
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset

class CommandRegistrator {

    private val logger: Logger = LoggerFactory.getLogger(CommandRegistrator::class.java)
    private val commands = mutableMapOf<String, ICommand>()
    private val cooldowns = mutableMapOf<Long, HashMap<ICommand, LocalDateTime>>()

    fun registerCommands(handlerID: String, commandsBasePackage: String) {
        val start = System.currentTimeMillis()

        ClassGraph().enableClassInfo().enableAnnotationInfo().acceptPackages(commandsBasePackage).scan().use { scanResult ->
            scanResult.getClassesWithAnnotation("net.bruhitsalex.sjch.annotations.Command").forEach { classInfo ->
                val commandName = classInfo
                    .getAnnotationInfo("net.bruhitsalex.sjch.annotations.Command")
                    .parameterValues.getValue("name") as String

                val commandHandlerId = classInfo
                    .getAnnotationInfo("net.bruhitsalex.sjch.annotations.Command")
                    .parameterValues.getValue("handlerId") as String

                if (commandHandlerId != handlerID) return@forEach

                var cooldown = 5
                var rolesRequired: List<String> = listOf()

                if (classInfo.hasAnnotation("net.bruhitsalex.sjch.annotations.Cooldown")) {
                    cooldown = classInfo
                        .getAnnotationInfo("net.bruhitsalex.sjch.annotations.Cooldown")
                        .parameterValues.getValue("time") as Int
                }

                if (classInfo.hasAnnotation("net.bruhitsalex.sjch.annotations.RolesRequired")) {
                    rolesRequired = classInfo
                        .getAnnotationInfo("net.bruhitsalex.sjch.annotations.RolesRequired")
                        .parameterValues.getValue("roles") as List<String>
                }

                val executeFunction = classInfo.loadClass().methods.find { it.name == "execute" }
                if (executeFunction == null) {
                    logger.error("Command $commandName does not have an execute function!")
                    return@forEach
                }

                val acceptedParams = mutableListOf<AcceptedParams>()
                for (parameter in executeFunction.parameters) {
                    when (parameter.type) {
                        MessageReceivedEvent::class.java -> acceptedParams.add(AcceptedParams.EVENT)
                        List::class.java -> acceptedParams.add(AcceptedParams.ARGS)
                        Member::class.java -> acceptedParams.add(AcceptedParams.AUTHOR)
                        MessageChannelUnion::class.java -> acceptedParams.add(AcceptedParams.CHANNEL)
                        JDA::class.java -> acceptedParams.add(AcceptedParams.BOT)
                        EmbedBuilder::class.java -> acceptedParams.add(AcceptedParams.DEFAULT_EMBED)
                        else -> {
                            logger.error("Command $commandName has an invalid parameter type: ${parameter.type}")
                            return@forEach
                        }
                    }
                }

                val kFunction = classInfo.loadClass().kotlin.members.find { it.name == "execute" }

                val command = ICommand(commandName, cooldown, rolesRequired, kFunction, acceptedParams)
                commands[commandName] = command
                logger.info("Registered command $commandName for handler $handlerID")
            }
        }

        logger.info("Registered ${commands.size} commands for handler $handlerID")
        logger.info("Took ${System.currentTimeMillis() - start}ms")
    }

    fun getCommand(command: String): ICommand? {
        return commands[command]
    }

    fun hasCooldown(member: Member, command: ICommand): Boolean {
        if (member.idLong !in cooldowns) {
            cooldowns[member.idLong] = hashMapOf()
        }

        val cooldownMap = cooldowns[member.idLong]!!

        if (command !in cooldownMap) {
            cooldownMap[command] = LocalDateTime.now()
            return false
        }

        val cooldownTime = cooldownMap[command]!!
        val now = LocalDateTime.now()

        if (now.isAfter(cooldownTime.plusSeconds(command.cooldown.toLong()))) {
            cooldownMap[command] = now
            return false
        }

        return true
    }

    fun hasRequiredRoles(member: Member, cmd: ICommand): Boolean {
        if (cmd.rolesRequired.isEmpty()) return true

        for (role in cmd.rolesRequired) {
            if (member.roles.any { it.id == role }) return true
        }

        return false
    }

    /**
     * Seconds left until cooldown is over
     */
    fun getCooldownLeft(member: Member, cmd: ICommand): Int {
        if (member.idLong !in cooldowns) {
            cooldowns[member.idLong] = hashMapOf()
        }

        val cooldownMap = cooldowns[member.idLong]!!

        if (cmd !in cooldownMap) {
            cooldownMap[cmd] = LocalDateTime.now()
            return 0
        }

        val cooldownTime = cooldownMap[cmd]!!
        val now = LocalDateTime.now()

        if (now.isAfter(cooldownTime.plusSeconds(cmd.cooldown.toLong()))) {
            cooldownMap[cmd] = now
            return 0
        }

        return (cooldownTime.plusSeconds(cmd.cooldown.toLong()).toEpochSecond(ZoneOffset.UTC) - now.toEpochSecond(ZoneOffset.UTC)).toInt()
    }

    fun setCooldown(member: Member, cmd: ICommand) {
        if (member.idLong !in cooldowns) {
            cooldowns[member.idLong] = hashMapOf()
        }

        val cooldownMap = cooldowns[member.idLong]!!

        cooldownMap[cmd] = LocalDateTime.now()
    }

}