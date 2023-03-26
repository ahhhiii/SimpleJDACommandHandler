package net.bruhitsalex.sjch

import net.bruhitsalex.sjch.types.AcceptedParams
import kotlin.reflect.KCallable

open class ICommand(
    val name: String,
    val cooldown: Int,
    val rolesRequired: List<String>,
    val executeFunction: KCallable<*>?,
    val acceptedParams: List<AcceptedParams>
)