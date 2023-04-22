package net.bruhitsalex.sjch

import io.github.classgraph.ClassGraph
import io.github.classgraph.ClassInfo
import net.dv8tion.jda.api.entities.Member
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneOffset

class CommandRegistrator {

    private val logger: Logger = LoggerFactory.getLogger(CommandRegistrator::class.java)
    val commands = mutableMapOf<String, ICommand>()
    private val cooldowns = mutableMapOf<Long, HashMap<ICommand, LocalDateTime>>()

    fun registerCommands(handlerID: String, commandsBasePackage: String) {
        val start = System.currentTimeMillis()

        ClassGraph().enableClassInfo().enableAnnotationInfo().acceptPackages(commandsBasePackage).scan().use { scanResult ->
            scanResult.getClassesWithAnnotation("net.bruhitsalex.sjch.annotations.Command").forEach { classInfo ->
                registerCommand(classInfo, handlerID)
            }
        }

        logger.info("Registered ${commands.size} commands for handler $handlerID")
        logger.info("Took ${System.currentTimeMillis() - start}ms")
    }

    private fun registerCommand(classInfo: ClassInfo, handlerID: String) {
        val commandInfo = classInfo.getAnnotationInfo("net.bruhitsalex.sjch.annotations.Command")

        val commandName= commandInfo.parameterValues.getValue("name") as String
        val commandHandlerId = commandInfo.parameterValues.getValue("handlerId") as String

        if (commandHandlerId != handlerID) return

        val description: String? = getClassAnnotationValue(classInfo, "net.bruhitsalex.sjch.annotations.Description", "description")
        val usage: String? = getClassAnnotationValue(classInfo, "net.bruhitsalex.sjch.annotations.Usage", "usage")
        val cooldown: Int? = getClassAnnotationValue(classInfo, "net.bruhitsalex.sjch.annotations.Cooldown", "time")
        val rolesRequired: Array<String>? = getClassAnnotationValue(classInfo, "net.bruhitsalex.sjch.annotations.RolesRequired", "roles")
        val channelsRequired: Array<String>? = getClassAnnotationValue(classInfo, "net.bruhitsalex.sjch.annotations.RestrictToChannels", "channels")

        val executeFunction = classInfo.loadClass().methods.find { it.name == "execute" }
        if (executeFunction == null) {
            logger.error("Command $commandName does not have an execute function!")
            return
        }

        val instance = classInfo.loadClass().getConstructor().newInstance()

        val command = ICommand(commandName, description, usage, cooldown, rolesRequired?.toList(), channelsRequired?.toList(), executeFunction, instance)
        commands[commandName] = command
        logger.info("Registered command $commandName for handler $handlerID")
    }

    fun getCommand(command: String): ICommand? {
        return commands[command]
    }

    fun hasCooldown(member: Member, command: ICommand): Boolean {
        if (command.cooldown == null) return false

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
        if (cmd.rolesRequired == null) return true

        for (role in cmd.rolesRequired) {
            if (member.roles.any { it.id == role }) return true
        }

        return false
    }

    /**
     * Seconds left until cooldown is over
     */
    fun getCooldownLeft(member: Member, cmd: ICommand): Int {
        if (cmd.cooldown == null) return 0

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

    private fun <T> getClassAnnotationValue(classInfo: ClassInfo, annotationClass: String, parameterName: String): T? {
        return if (classInfo.hasAnnotation(annotationClass)) {
            classInfo
                .getAnnotationInfo(annotationClass)
                .parameterValues.getValue(parameterName) as T
        } else {
            null
        }
    }

}