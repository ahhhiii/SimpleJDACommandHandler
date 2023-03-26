package testbot.commands

import net.bruhitsalex.sjch.annotations.Command
import net.bruhitsalex.sjch.annotations.Cooldown
import net.bruhitsalex.sjch.annotations.RolesRequired
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion

@Command("ping")
@Cooldown(5)
@RolesRequired(["871537621065670656"])
class PongCommand {

    fun execute(channel: MessageChannelUnion) {
        channel.sendMessage("Pong!").queue()
    }

}