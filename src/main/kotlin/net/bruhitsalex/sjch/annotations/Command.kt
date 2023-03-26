package net.bruhitsalex.sjch.annotations

import net.bruhitsalex.sjch.defaultHandlerId

annotation class Command(
    val name: String,
    val handlerId: String = defaultHandlerId
)
