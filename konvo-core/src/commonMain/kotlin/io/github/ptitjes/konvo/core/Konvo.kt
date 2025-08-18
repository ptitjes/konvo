package io.github.ptitjes.konvo.core

import io.github.ptitjes.konvo.core.ai.prompts.*
import io.github.ptitjes.konvo.core.ai.tools.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.models.*
import kotlinx.coroutines.flow.*
import org.kodein.di.*

class Konvo(
    override val di: DI,
) : DIAware {
    private val modelProviderManager: ModelManager by instance()
    private val promptManager: PromptManager by instance()
    private val toolProviders: io.github.ptitjes.konvo.core.ai.tools.ToolManager by instance()
    private val characterProviderManager: CharacterManager by instance()

    private var _models: List<ModelCard> = emptyList()
    private var _prompts: List<PromptCard> = emptyList()
    private var _tools: List<ToolCard> = emptyList()
    private var _characters: List<CharacterCard> = emptyList()

    suspend fun init() {
        _models = modelProviderManager.models.first()
        _prompts = promptManager.prompts.first()
        _tools = toolProviders.tools.first()
        _characters = characterProviderManager.characters.first()
    }

    val models: List<ModelCard> get() = _models
    val prompts: List<PromptCard> get() = _prompts
    val tools: List<ToolCard> get() = _tools
    val characters: List<CharacterCard> get() = _characters
}
