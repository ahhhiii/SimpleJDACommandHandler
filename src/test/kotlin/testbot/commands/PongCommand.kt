package testbot.commands

import net.bruhitsalex.sjch.annotations.Command
import net.bruhitsalex.sjch.annotations.Cooldown
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

@Command("ping")
@Cooldown(5)
class PongCommand {

    fun execute(channel: TextChannel) {
        channel.sendMessage("Pong!").queue()
    }

}