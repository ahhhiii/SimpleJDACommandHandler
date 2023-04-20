package net.bruhitsalex.sjch

import java.lang.reflect.Method

class ICommand(
    val name: String,
    val description: String?,
    val usage: String?,
    val cooldown: Int?,
    val rolesRequired: List<String>?,
    val channelsRequired: List<String>?,
    val executeFunction: Method,
    val instance: Any
) {

    init {
        executeFunction.isAccessible = true
    }

}