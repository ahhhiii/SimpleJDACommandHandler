package testbot.commands

import net.bruhitsalex.sjch.annotations.*
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel

@Command("ping")
@Description("Replies with 'Pong!'")
@RolesRequired(["1097870977339232347"])
@Cooldown(5)
class PongCommand {

    fun execute(channel: TextChannel) {
        channel.sendMessage("Pong!").queue()
    }

}