package io.github.ptitjes.konvo.core.models.providers

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.executor.clients.mistralai.*
import ai.koog.prompt.executor.clients.openai.base.*
import ai.koog.prompt.llm.*
import io.github.ptitjes.konvo.core.models.*
import io.ktor.client.*
import kotlinx.serialization.*
import kotlin.time.*

class MistralAIModelProvider(
    override val name: String,
    apiKey: String,
) : ModelProvider {
    private val client by lazy { CustomMistralAILLMClient(apiKey) }

    override suspend fun queryModels(): List<ModelCard> {
        // Name                 Speed       Context     Input                   Output          Price
        // MistralLarge21	    Medium	    128K	    Text, Tools	            Text, Tools     $2-$8
        // MistralMedium31	    Medium	    128K	    Text, Images, Tools	    Text, Tools     $0.4-$2
        // MagistralMedium12	Medium	    128K	    Text, Images, Tools	    Text, Tools     $0.4-$2
        // DevstralMedium	    Medium	    128K	    Text, Tools	            Text, Tools     $0.4-$2
        // MistralSmall2	    Fast	    32K     	Text, Tools         	Text, Tools     $0.2-$0.6
        // Codestral	        Fast	    256K	    Text, Tools	            Text, Tools     $0.2-$0.6
        // Ministral3B	        Fast	    128K	    Text, Tools	            Text, Tools     $0.04-$0.16

        return client.models().map { model ->
            MistralAIModelCard(delegate = model, name = modelIdToPrettyName(model.id))
        }
    }

    private fun modelIdToPrettyName(id: String): String = id
        .removeSuffix("-latest")
        .split('-')
        .joinToString(" ") {
            it.replaceFirstChar(Char::uppercaseChar)
        }

    private inner class MistralAIModelCard(
        private val delegate: LLModel,
        override val name: String,
    ) : ModelCard {
        override val provider: ModelProvider get() = this@MistralAIModelProvider
        override val size: Long? get() = null
        override val parameterCount: Long? get() = null
        override val contextLength: Long? get() = delegate.contextLength
        override val quantizationLevel: String? get() = null
        override val supportsTools: Boolean get() {
            val capabilities = delegate.capabilities
            return capabilities != null && LLMCapability.Tools in capabilities
        }

        override fun toLLModel(): LLModel = delegate
        override fun getLLMClient(): LLMClient = this@MistralAIModelProvider.client
    }
}

private class CustomMistralAILLMClient(
    apiKey: String,
    private val settings: MistralAIClientSettings = MistralAIClientSettings(),
    baseClient: HttpClient = HttpClient(),
    clock: Clock = Clock.System,
    toolsConverter: OpenAICompatibleToolDescriptorSchemaGenerator = OpenAICompatibleToolDescriptorSchemaGenerator(),
) : MistralAILLMClient(
    apiKey,
    settings,
    baseClient,
    clock,
    toolsConverter
) {
    override suspend fun models(): List<LLModel> {
        @Suppress("UnstableApiUsage")
        val models = httpClient.get(
            path = settings.modelsPath,
            responseType = MistralModelsResponse::class
        )

        val selectedModels = models.data
            .filter { it.deprecation == null }
            .filter { it.capabilities.completionChat }
            .filter { modelIdHasDateSuffix(it.id) }

        val modelsById = patchedModelsById()

        return selectedModels.mapNotNull { model ->
            val hardcodedModel = modelsById[model.id]

            val queriedModel = LLModel(
                provider = llmProvider(),
                id = model.id,
                capabilities = model.capabilities.toKoogCapabilities(),
                contextLength = model.maxContextLength.toLong(),
            )
            if (hardcodedModel != null) println("> Kept: $hardcodedModel")
            else println("> Ignored: $queriedModel")

            hardcodedModel
        }
    }

    private fun modelIdHasDateSuffix(id: String): Boolean {
        val suffix = id.takeLast(5)
        return suffix[0] != '-' || !(1..4).all { suffix[it].isDigit() }
    }

    private fun patchedModelsById(): Map<String, LLModel> {
        return MistralAIModels.modelsById() + mapOf(
            MistralAIModels.Chat.MistralLarge21.id to MistralAIModels.Chat.MistralLarge21.copy(
                contextLength = 256_000,
                capabilities = MistralAIModels.Chat.MistralLarge21.capabilities!! +
                        LLMCapability.Vision.Image,
            ),
            MistralAIModels.Chat.MistralSmall2.id to MistralAIModels.Chat.MistralSmall2.copy(
                contextLength = 128_000,
                capabilities = MistralAIModels.Chat.MistralSmall2.capabilities!! +
                        LLMCapability.Vision.Image,
            ),
            "ministral-3b-latest" to LLModel(
                provider = LLMProvider.MistralAI,
                id = "ministral-3b-latest",
                capabilities = listOf(
                    LLMCapability.Temperature,
                    LLMCapability.Completion,
                    LLMCapability.Tools,
                    LLMCapability.ToolChoice,
                    LLMCapability.Schema.JSON.Basic,
                    LLMCapability.Vision.Image,
                    LLMCapability.Document,
                ),
                contextLength = 256_000
            ),
            "ministral-8b-latest" to LLModel(
                provider = LLMProvider.MistralAI,
                id = "ministral-8b-latest",
                capabilities = listOf(
                    LLMCapability.Temperature,
                    LLMCapability.Completion,
                    LLMCapability.Tools,
                    LLMCapability.ToolChoice,
                    LLMCapability.Schema.JSON.Basic,
                    LLMCapability.Vision.Image,
                    LLMCapability.Document,
                ),
                contextLength = 256_000
            ),
            "ministral-14b-latest" to LLModel(
                provider = LLMProvider.MistralAI,
                id = "ministral-14b-latest",
                capabilities = listOf(
                    LLMCapability.Temperature,
                    LLMCapability.Completion,
                    LLMCapability.Tools,
                    LLMCapability.ToolChoice,
                    LLMCapability.Schema.JSON.Basic,
                    LLMCapability.Vision.Image,
                    LLMCapability.Document,
                ),
                contextLength = 256_000
            ),
        )
    }
}

private fun MistralModelCapabilities.toKoogCapabilities(): List<LLMCapability> = buildList {
    if (audio || audioTranscription) add(LLMCapability.Audio)
    if (completionChat) add(LLMCapability.Completion)
    if (functionCalling) add(LLMCapability.Tools)
    if (moderation) add(LLMCapability.Moderation)
    if (vision) add(LLMCapability.Vision.Image)
}

@Serializable
private data class MistralModelsResponse(
    val data: List<MistralModel>,
)

@Serializable
private data class MistralModelCapabilities(
    val audio: Boolean = false,
    @SerialName("audio_transcription")
    val audioTranscription: Boolean = false,
    val classification: Boolean = false,
    @SerialName("completion_chat")
    val completionChat: Boolean = false,
    @SerialName("completion_fim")
    val completionFim: Boolean = false,
    @SerialName("fine_tuning")
    val fineTuning: Boolean = false,
    @SerialName("function_calling")
    val functionCalling: Boolean = false,
    @SerialName("moderation")
    val moderation: Boolean = false,
    @SerialName("ocr")
    val ocr: Boolean = false,
    @SerialName("vision")
    val vision: Boolean = false,
    val version: Boolean = false,
)

@Serializable
private data class MistralModel(
    val aliases: Set<String> = emptySet(),
    val capabilities: MistralModelCapabilities,
    val deprecation: Instant? = null,
    val description: String? = null,
    val id: String,
    @SerialName("max_context_length")
    val maxContextLength: Int = 32768,
    val name: String? = null,
    @SerialName("type")
    val objectType: String = "base",
    @SerialName("owned_by")
    val ownedBy: String = "mistralai",
)
