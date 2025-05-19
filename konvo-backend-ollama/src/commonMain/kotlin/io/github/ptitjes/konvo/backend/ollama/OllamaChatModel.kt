package io.github.ptitjes.konvo.backend.ollama

import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.ai.spi.Tool
import io.github.ptitjes.konvo.core.ai.spi.ToolCall
import kotlinx.coroutines.*
import org.nirmato.ollama.api.*
import org.nirmato.ollama.api.ChatRequest.Companion.chatRequest
import org.nirmato.ollama.api.EmbedRequest.Companion.embedRequest
import org.nirmato.ollama.client.ktor.*
import org.nirmato.ollama.api.Tool as OTool
import org.nirmato.ollama.api.ToolCall as OToolCall

internal class OllamaChatModel(
    private val client: OllamaClient,
    private val modelCard: ModelCard,
) : ChatModel {
    override suspend fun withTokenCount(
        message: ChatMessage,
    ): ChatMessage {
        if (message is ChatMessage.Assistant) {
            if (message.metadata == null) error("Assistant message should already have metadata")
            return message
        }

        val text = when (message) {
            is ChatMessage.System -> message.text
            is ChatMessage.User -> message.text
            is ChatMessage.Tool -> message.result.contentString(message.call)
            else -> error("Invalid state")
        }

        // FIXME we temporarily use embedding generation to count the tokens
        // Can be fixed when https://github.com/ollama/ollama/issues/3582 is closed
        val response = client.generateEmbed(
            embedRequest {
                model(modelCard.name)
                this.input(EmbeddedInput.EmbeddedText(text))
            }
        )

        // TODO determine if these fields are always set
        val promptEvalCount = response.promptEvalCount!!
        val metadata = ChatMessage.Metadata(promptEvalCount)

        return when (message) {
            is ChatMessage.System -> message.copy(metadata = metadata)
            is ChatMessage.User -> message.copy(metadata = metadata)
            is ChatMessage.Tool -> message.copy(metadata = metadata)
            else -> error("Invalid state")
        }
    }

    override suspend fun chat(
        context: List<ChatMessage>,
        tools: List<Tool>?,
    ): ChatMessage.Assistant = withContext(Dispatchers.IO) {
        if (tools != null && !modelCard.supportsTools) error("This model does not support tools")

        val messages = context.map { it.toOllamaMessage() }

        val ollamaResponse = client.chat(
            chatRequest {
                model(modelCard.name)
                messages(messages)
                if (tools != null) {
                    tools(tools.map { it.toOllamaToolSpecification() })
                }
            }
        )

        val message = ollamaResponse.message ?: error("Model returned an empty response")
        if (message.role != Role.ASSISTANT) error("Model did not return a message with assistant role")

        // TODO determine if these fields are always set
        val evalCount = ollamaResponse.evalCount!!

        ChatMessage.Assistant(
            text = message.content,
            toolCalls = message.tools?.map {
                ToolCall(
                    name = it.function.name,
                    arguments = it.function.arguments,
                )
            },
            metadata = ChatMessage.Metadata(tokenCount = evalCount),
        )
    }

    private fun ChatMessage.toOllamaMessage(): Message = when (this) {
        is ChatMessage.System -> Message(role = Role.SYSTEM, content = this.text)
        is ChatMessage.User -> Message(role = Role.USER, content = this.text)
        is ChatMessage.Tool -> Message(role = Role.TOOL, content = result.contentString(call))
        is ChatMessage.Assistant -> Message(
            role = Role.ASSISTANT,
            content = this.text, tools = this.toolCalls?.map {
                OToolCall(
                    function = OToolCall.FunctionCall(
                        name = it.name,
                        arguments = it.arguments,
                    )
                )
            }
        )
    }

    private fun Tool.toOllamaToolSpecification(): OTool {
        return OTool(
            type = "function",
            function = OTool.ToolFunction(
                name = this.name,
                description = description,
                parameters = OTool.ToolParameters(
                    properties = parameters.properties,
                    required = parameters.required,
                )
            )
        )
    }
}
