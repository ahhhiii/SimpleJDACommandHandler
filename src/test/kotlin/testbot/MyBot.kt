package testbot

import net.bruhitsalex.sjch.ICommandHandler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent

var bot: JDA? = null

fun main() {
    val token = System.getenv("TOKEN")
    bot = JDABuilder.createDefault(token)
        .addEventListeners(ICommandHandler(bot, "testbot.commands"))
        .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
        .build()
}