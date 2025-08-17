package io.github.ptitjes.konvo.core

import io.github.ptitjes.konvo.core.ai.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.models.*
import org.kodein.di.*

class Konvo(
    override val di: DI,
) : DIAware {
    private val modelProviderManager: ModelManager by instance()
    private val promptProviderManager: ProviderManager<PromptCard> by instance()
    private val toolProviders: ProviderManager<ToolCard> by instance()
    private val characterProviderManager: CharacterCardManager by instance()

    val models: List<Model> get() = modelProviderManager.elements
    val prompts: List<PromptCard> get() = promptProviderManager.elements
    val tools: List<ToolCard> get() = toolProviders.elements
    val characters: List<CharacterCard> get() = characterProviderManager.elements
}
