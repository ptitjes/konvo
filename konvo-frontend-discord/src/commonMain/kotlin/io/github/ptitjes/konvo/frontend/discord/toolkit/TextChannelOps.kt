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
}

fun CoroutineScope.typingToggler(channel: MessageChannelBehavior): TypingToggler = object : TypingToggler {
    private var typingJob: Job? = null

    override suspend fun start() {
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
    }
}
