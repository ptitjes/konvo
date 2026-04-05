package io.github.ptitjes.konvo.plugin.roleplay

fun String.replaceTags(userName: String, characterName: String): String = this
    .replace("<user>", userName, true)
    .replace("{{user}}", userName, true)
    .replace("<bot>", characterName, true)
    .replace("{{char}}", characterName, true)