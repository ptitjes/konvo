package io.github.ptitjes.konvo.core.ai.spi

sealed interface ChatMessage {
    val metadata: Metadata?

    data class System(
        val text: String,
        override val metadata: Metadata? = null,
    ) : ChatMessage

    data class User(
        val text: String,
        override val metadata: Metadata? = null,
    ) : ChatMessage

    data class Assistant(
        val text: String,
        val toolCalls: List<ToolCall>? = null,
        override val metadata: Metadata? = null,
    ) : ChatMessage {
        fun hasToolCalls() = !toolCalls.isNullOrEmpty()
    }

    data class Tool(
        val call: ToolCall,
        val result: ToolCallResult,
        override val metadata: Metadata? = null,
    ) : ChatMessage

    data class Metadata(
        val tokenCount: Int,
    )
}
