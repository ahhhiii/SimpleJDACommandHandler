package net.bruhitsalex.sjch.types

enum class AcceptedParams {
    CHANNEL, // MessageChannelUnion
    AUTHOR, // Member
    EVENT, // MessageReceivedEvent
    ARGS, // List<String>
    BOT, // JDA
    DEFAULT_EMBED, // EmbedBuilder
}