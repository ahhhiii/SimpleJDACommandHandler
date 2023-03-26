package net.bruhitsalex.sjch.utils

import java.awt.Color

object Utils {

    fun formatDuration(cooldownInSeconds: Int): String {
        val hours = cooldownInSeconds / 3600
        val minutes = (cooldownInSeconds % 3600) / 60
        val seconds = cooldownInSeconds % 60

        return when {
            hours > 0 -> {
                "${hours}h ${minutes}m ${seconds}s"
            }
            minutes > 0 -> {
                "${minutes}m ${seconds}s"
            }
            else -> {
                "${seconds}s"
            }
        }
    }

    val GREEN = Color.decode("#7ad33a")
    val RED = Color.decode("#d33f3a")
    val YELLOW = Color.decode("#e8e238")

}