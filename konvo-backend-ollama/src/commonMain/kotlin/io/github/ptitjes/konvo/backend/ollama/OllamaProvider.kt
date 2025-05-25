package io.github.ptitjes.konvo.backend.ollama

import io.github.ptitjes.konvo.core.ai.spi.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.nirmato.ollama.api.*
import org.nirmato.ollama.client.ktor.*

class OllamaProvider(private val urlString: String = "http://localhost:11434") : ModelProvider {
    override val name: String get() = "Ollama"

    private val client = OllamaClient(CIO) {
        defaultRequest {
            url("$urlString/api")
        }
        install(HttpTimeout) {
            requestTimeoutMillis = Long.MAX_VALUE
        }
    }

    override suspend fun queryModelCards(): List<ModelCard> = withContext(Dispatchers.IO) {
        val listModelsResponse = client.listModels()

        listModelsResponse.models?.mapNotNull { model ->
            val name = model.name
            if (name == null) return@mapNotNull null

            val size = model.size
            val parameterSize = model.details?.parameterSize
            val quantizationLevel = model.details?.quantizationLevel

            val showModelResponse = client.showModel(ShowModelRequest(name = name))

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

    override fun newChatModel(modelCard: ModelCard): ChatModel {
        checkModelProvider(modelCard)
        return OllamaChatModel(client, modelCard)
    }

    private fun checkModelProvider(modelCard: ModelCard) {
        if (modelCard.provider != this) error("This model is not provided by Ollama")
    }
}
