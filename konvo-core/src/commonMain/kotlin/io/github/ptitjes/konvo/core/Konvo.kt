package io.github.ptitjes.konvo.core

import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.prompts.*
import io.github.ptitjes.konvo.core.roleplay.*
import kotlinx.coroutines.flow.*
import org.kodein.di.*

class Konvo(
    override val di: DI,
) : DIAware {
    private val modelProviderManager: ModelManager by instance()
    private val promptManager: PromptManager by instance()
    private val mcpServerSpecificationsManager: McpServerSpecificationsManager by instance()
    private val characterProviderManager: CharacterManager by instance()

    private var _models: List<ModelCard> = emptyList()
    private var _prompts: List<PromptCard> = emptyList()
    private var _mcpServerNames: Set<String> = emptySet()
    private var _characters: List<CharacterCard> = emptyList()

    suspend fun init() {
        _models = modelProviderManager.models.first()
        _prompts = promptManager.prompts.first()
        _mcpServerNames = mcpServerSpecificationsManager.specifications.first().keys
        _characters = characterProviderManager.characters.first()
    }

    val models: List<ModelCard> get() = _models
    val prompts: List<PromptCard> get() = _prompts
    val mcpServerNames: Set<String> get() = _mcpServerNames
    val characters: List<CharacterCard> get() = _characters
}
