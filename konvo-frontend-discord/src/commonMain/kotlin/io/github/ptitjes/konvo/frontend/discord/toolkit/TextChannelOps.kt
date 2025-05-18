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

suspend fun MessageChannelBehavior.typingToggler(): TypingToggler = coroutineScope {
    val coroutineScope = this
    object : TypingToggler {
        private var typingJob: Job? = null

        override suspend fun start() {
            if (typingJob == null) {
                type()
                typingJob = coroutineScope.launch {
                    try {
                        while (true) {
                            delay(8.seconds)
                            type()
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
}
