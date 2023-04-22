package testbot.commands

import net.bruhitsalex.sjch.ICommandHandler
import net.bruhitsalex.sjch.annotations.Command
import net.bruhitsalex.sjch.annotations.Description
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

@Command("help")
@Description("Shows a list of commands")
class HelpCommand {

    fun execute(channel: TextChannel, cmdHandler: ICommandHandler) {
        val nameToCommand = cmdHandler.commandRegistrator.commands
        val builder = StringBuilder()
        for ((name, iCommand) in nameToCommand) {
            builder.append("**${name}** - ${iCommand.description}\n")
        }
        channel.sendMessage(builder.toString()).queue()
    }

}