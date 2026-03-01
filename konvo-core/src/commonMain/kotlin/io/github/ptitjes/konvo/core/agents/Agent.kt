package io.github.ptitjes.konvo.core.agents

import ai.koog.prompt.dsl.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.mcp.*

interface Agent {
    suspend fun restoreSession(
        transcript: ConversationTranscript,
        invite: Action<ConversationControl.InviteAgent>,
        device: InteractionDevice.Agent,
    ): AgentSession
}

interface AgentSession {
    val isPaused: Boolean
    suspend fun pause()
    suspend fun resume()
    suspend fun close()
}

interface AgentContext {
    val prompt: Prompt
    fun resetPrompt(prompt: Prompt)
    fun resetPrompt(builder: PromptBuilder.() -> Unit)
    fun appendToPrompt(builder: PromptBuilder.() -> Unit)

    suspend fun <T> withMcpSession(block: suspend (McpHostSession) -> T): T
}
