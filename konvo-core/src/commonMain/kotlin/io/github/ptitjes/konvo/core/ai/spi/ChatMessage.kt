package io.github.ptitjes.konvo.core.ai.spi

sealed interface ChatMessage {
    data class System(
        val text: String,
    ) : ChatMessage

    data class User(
        val text: String,
    ) : ChatMessage

    data class Assistant(
        val text: String,
        val toolCalls: List<ToolCall>? = null,
    ) : ChatMessage {
        fun hasToolCalls() = !toolCalls.isNullOrEmpty()
    }

    data class Tool(
        val call: ToolCall,
        val result: ToolCallResult,
    ) : ChatMessage
}
