package io.github.ptitjes.konvo.frontend.discord.toolkit

import dev.kord.core.behavior.channel.*
import dev.kord.core.entity.channel.*
import kotlinx.coroutines.*
import kotlin.time.Duration.Companion.seconds

val TextChannel.link: String get() = "[#$name]($url)"
val TextChannel.url: String get() = "https://discord.com/channels/${guildId}/${id}"

interface TypingToggler {
    suspend fun start()
    suspend fun stop()
    suspend fun maybeRestart()
}

fun CoroutineScope.typingToggler(channel: MessageChannelBehavior): TypingToggler = object : TypingToggler {
    private var isTyping = false
    private var typingJob: Job? = null

    override suspend fun start() {
        isTyping = true

        if (typingJob == null) {
            channel.type()
            typingJob = launch {
                try {
                    while (isActive) {
                        delay(8.seconds)
                        channel.type()
                    }
                } finally {
                    typingJob = null
                }
            }
        }
    }

    override suspend fun stop() {
        typingJob?.cancel()
        typingJob = null
        isTyping = false
    }

    override suspend fun maybeRestart() {
        if (isTyping) start()
    }
}
