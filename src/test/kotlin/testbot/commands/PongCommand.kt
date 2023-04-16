package testbot.commands

import net.bruhitsalex.sjch.annotations.Command
import net.bruhitsalex.sjch.annotations.Cooldown
import net.dv8tion.jda.internal.entities.channel.concrete.TextChannelImpl

@Command("ping")
@Cooldown(5)
object PongCommand {

    fun execute(channel: TextChannelImpl) {
        channel.sendMessage("Pong!").queue()
    }

}