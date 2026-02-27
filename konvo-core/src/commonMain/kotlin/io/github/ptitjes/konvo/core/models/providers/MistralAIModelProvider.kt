package io.github.ptitjes.konvo.core.models.providers

import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.executor.clients.mistralai.*
import ai.koog.prompt.llm.*
import io.github.ptitjes.konvo.core.models.*

class MistralAIModelProvider(
    override val name: String,
    apiKey: String,
) : ModelProvider {
    private val client by lazy { MistralAILLMClient(apiKey) }

    override suspend fun queryModels(): List<ModelCard> {

        // Name                 Speed       Context     Input                   Output          Price
        // MistralLarge21	    Medium	    128K	    Text, Tools	            Text, Tools     $2-$8
        // MistralMedium31	    Medium	    128K	    Text, Images, Tools	    Text, Tools     $0.4-$2
        // MagistralMedium12	Medium	    128K	    Text, Images, Tools	    Text, Tools     $0.4-$2
        // DevstralMedium	    Medium	    128K	    Text, Tools	            Text, Tools     $0.4-$2
        // MistralSmall2	    Fast	    32K     	Text, Tools         	Text, Tools     $0.2-$0.6
        // Codestral	        Fast	    256K	    Text, Tools	            Text, Tools     $0.2-$0.6
        // Ministral3B	        Fast	    128K	    Text, Tools	            Text, Tools     $0.04-$0.16

        return listOf(
            MistralAIModels.Chat.MistralLarge21 to "Mistral Large",
            MistralAIModels.Chat.MistralMedium31 to "Mistral Medium",
            MistralAIModels.Chat.MagistralMedium12 to "Magistral Medium",
            MistralAIModels.Chat.DevstralMedium to "Devstral Medium",
            MistralAIModels.Chat.MistralSmall2 to "Mistral Small",
            MistralAIModels.Chat.Codestral to "Codestral",
//            MistralAIModels.Chat.Ministral3B to "Ministral",
        ).map { (model, name) -> MistralAIModelCard(model, name) }
    }

    private inner class MistralAIModelCard(
        private val delegate: LLModel,
        override val name: String,
    ) : ModelCard {
        override val provider: ModelProvider get() = this@MistralAIModelProvider
        override val size: Long? get() = null
        override val parameterCount: Long? get() = null
        override val contextLength: Long? get() = null
        override val quantizationLevel: String? get() = null
        override val supportsTools: Boolean get() = LLMCapability.Tools in delegate.capabilities

        override fun toLLModel(): LLModel = delegate
        override fun getLLMClient(): LLMClient = this@MistralAIModelProvider.client
    }
}
