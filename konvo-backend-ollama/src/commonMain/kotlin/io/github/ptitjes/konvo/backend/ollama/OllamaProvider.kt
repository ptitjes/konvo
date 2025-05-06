package io.github.ptitjes.konvo.backend.ollama

import io.github.ptitjes.konvo.core.spi.*
import io.github.ptitjes.konvo.core.spi.Tool
import io.github.ptitjes.konvo.core.spi.ToolCall
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import org.nirmato.ollama.api.*
import org.nirmato.ollama.client.ktor.*
import org.nirmato.ollama.api.Tool as OTool
import org.nirmato.ollama.api.ToolCall as OToolCall

class OllamaProvider(private val urlString: String) : ModelProvider {
    override val name: String get() = "Ollama"

    private val ollamaClient = OllamaClient(CIO) {
        defaultRequest {
            url(urlString)
        }
        install(HttpTimeout) {
            requestTimeoutMillis = Long.MAX_VALUE
        }
    }

    override suspend fun queryModels(): List<ModelCard> = withContext(Dispatchers.IO) {
        val listModelsResponse = ollamaClient.listModels()

        listModelsResponse.models?.mapNotNull { model ->
            val name = model.name
            if (name == null) return@mapNotNull null

            val size = model.size
            val parameterSize = model.details?.parameterSize
            val quantizationLevel = model.details?.quantizationLevel

            val showModelResponse = ollamaClient.showModel(ShowModelRequest(name = name))

            val modelInfo = showModelResponse.modelInfo
            val architecture = modelInfo?.get("general.architecture")?.jsonPrimitive?.contentOrNull
            val contextSize = architecture?.let { modelInfo["$it.context_length"]?.jsonPrimitive?.long }

            val capabilities = showModelResponse.capabilities
            val supportsTools = capabilities != null && ModelCapability.Tools in capabilities

            ModelCard(
                provider = this@OllamaProvider,
                name = name,
                size = size,
                parameterSize = parameterSize,
                quantizationLevel = quantizationLevel,
                contextSize = contextSize,
                supportsTools = supportsTools
            )
        }?.sortedBy { it.name } ?: emptyList()
    }

    private fun checkModelProvider(model: ModelCard) {
        if (model.provider != this) error("This model is not provided by Ollama")
    }

    override suspend fun preloadModel(modelCard: ModelCard) = withContext(Dispatchers.IO) {
        checkModelProvider(modelCard)

        ollamaClient.chatStream(
            ChatRequest.Companion.chatRequest {
                model(modelCard.name)
                messages(listOf())
            }
        ).collect()
    }

    override suspend fun chat(
        modelCard: ModelCard,
        context: List<ChatMessage>,
        tools: List<Tool>?,
    ): List<ChatMessage> = withContext(Dispatchers.IO) {
        checkModelProvider(modelCard)

        if (tools != null && !modelCard.supportsTools) error("This model does not support tools")

        val incomingContext = context.map { it.toOllamaMessage() }

        val ollamaResponse = ollamaClient.chat(
            ChatRequest.Companion.chatRequest {
                model(modelCard.name)
                messages(incomingContext)
                if (tools != null) {
                    tools(tools.map { it.toOllamaToolSpecification() })
                }
            }
        )

        val message = ollamaResponse.message ?: error("Model return an empty response")
        listOf(message.toKonvoMessage())
    }


    private fun Message.toKonvoMessage(): ChatMessage = when (role) {
        Role.ASSISTANT -> ChatMessage.Assistant(
            text = content, toolCalls = tools?.map {
                ToolCall(
                    name = it.function.name,
                    arguments = it.function.arguments,
                )
            }
        )

        else -> error("Illegal state")
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
