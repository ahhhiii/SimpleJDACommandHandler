package net.bruhitsalex.sjch

import java.lang.reflect.Method

class ICommand(
    val name: String,
    val cooldown: Int,
    val rolesRequired: List<String>,
    val executeFunction: Method
) {

    init {
        executeFunction.isAccessible = true
    }

}