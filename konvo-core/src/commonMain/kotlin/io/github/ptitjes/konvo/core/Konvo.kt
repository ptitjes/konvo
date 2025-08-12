package io.github.ptitjes.konvo.core

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.ai.spi.*
import kotlinx.coroutines.*
import org.kodein.di.*
import kotlin.coroutines.*

class Konvo(
    coroutineContext: CoroutineContext,
    override val di: DI,
) : DIAware {

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val job = SupervisorJob(coroutineContext[Job])
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception caught in Konvo" }
    }

    private val coroutineScope = CoroutineScope(coroutineContext + Dispatchers.Default + job + handler)

    private val modelProviders: Set<ModelProvider> by instance()
    private val promptProviders: Set<PromptProvider> by instance()
    private val toolProviders: Set<ToolProvider> by instance()
    private val characterProviders: Set<CharacterProvider> by instance()

    private lateinit var _models: List<ModelCard>;
    private lateinit var _prompts: List<PromptCard>;
    private lateinit var _tools: List<ToolCard>;
    private lateinit var _characters: List<CharacterCard>;

    suspend fun init() {
        logger.info { "Initializing Konvo" }

        _models = modelProviders.flatMap { it.queryModelCards() }.also {
            logger.info { "Loaded ${it.size} model cards" }
        }

        _prompts = promptProviders.flatMap { it.queryPrompts() }.also {
            logger.info { "Loaded ${it.size} prompt cards" }
        }

        _tools = toolProviders.flatMap { it.queryTools() }.also {
            logger.info { "Loaded ${it.size} tool cards" }
        }

        _characters = characterProviders.flatMap { it.queryCharacters() }.also {
            logger.info { "Loaded ${it.size} character cards" }
        }

        logger.info { "Initialized Konvo" }
    }

    val models: List<ModelCard> get() = _models
    val characters: List<CharacterCard> get() = _characters
    val prompts: List<PromptCard> get() = _prompts
    val tools: List<ToolCard> get() = _tools
}
