package io.github.ptitjes.konvo.core.spi

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
    ) : ChatMessage

    data class Tool(
        val call: ToolCall,
        val result: ToolCallResult,
    ) : ChatMessage
}
