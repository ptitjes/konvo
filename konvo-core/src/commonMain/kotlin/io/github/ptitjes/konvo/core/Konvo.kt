package io.github.ptitjes.konvo.core

import io.github.ptitjes.konvo.core.ai.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.models.*
import kotlinx.coroutines.flow.*
import org.kodein.di.*

class Konvo(
    override val di: DI,
) : DIAware {
    private val modelProviderManager: ModelManager by instance()
    private val promptProviderManager: ProviderManager<PromptCard> by instance()
    private val toolProviders: ProviderManager<ToolCard> by instance()
    private val characterProviderManager: CharacterManager by instance()

    private var _models: List<ModelCard> = emptyList()
    private var _characters: List<CharacterCard> = emptyList()

    suspend fun init() {
        // Ensure models are initialized by collecting the first emission and caching it
        _models = modelProviderManager.models.first()
        // Ensure characters are initialized by collecting the first emission and caching it
        _characters = characterProviderManager.characters.first()
    }

    val models: List<ModelCard> get() = _models
    val prompts: List<PromptCard> get() = promptProviderManager.elements
    val tools: List<ToolCard> get() = toolProviders.elements
    val characters: List<CharacterCard> get() = _characters
}
