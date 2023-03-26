package testbot

import net.bruhitsalex.sjch.ICommandHandler
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.JDABuilder

var bot: JDA? = null

fun main() {
   bot = JDABuilder.createDefault("token")
        .addEventListeners(ICommandHandler(bot!!, "testbot.commands"))
        .build()
}