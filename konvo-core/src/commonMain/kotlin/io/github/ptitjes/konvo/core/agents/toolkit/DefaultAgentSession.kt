package io.github.ptitjes.konvo.core.agents.toolkit

import io.github.ptitjes.konvo.core.agents.*

class DefaultAgentSession(
    private val context: DefaultAgentContext,
) : AgentSession {

    override val isPaused: Boolean
        get() = context.isPaused

    override suspend fun pause() = context.pause()

    override suspend fun resume() = context.resume()

    override suspend fun close() = context.close()
}
