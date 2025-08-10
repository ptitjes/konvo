package io.github.ptitjes.konvo.core

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.*
import kotlinx.coroutines.*
import org.kodein.di.*
import kotlin.coroutines.*

interface KonvoConfigurationBuilder {
    var dataDirectory: String
    fun installModels(provider: ModelProvider)
    fun installPrompts(provider: PromptProvider)
    fun installTools(provider: ToolProvider)
}

data class KonvoConfiguration(
    val dataDirectory: String,
    val modelProviders: List<ModelProvider>,
    val promptProviders: List<PromptProvider>,
    val toolProviders: List<ToolProvider>,
)

//fun CoroutineScope.Konvo(configure: KonvoConfigurationBuilder.() -> Unit): Konvo {
//    val configuration = buildKonvoConfiguration(configure)
//    return Konvo(coroutineContext + Dispatchers.Default, configuration)
//}

class Konvo(
    coroutineContext: CoroutineContext,
    override val di: DI,
) : CoroutineScope, DIAware {

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val job = SupervisorJob(coroutineContext[Job])
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception caught in Konvo" }
    }

    override val coroutineContext: CoroutineContext = coroutineContext + Dispatchers.Default + job + handler

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

    suspend fun createConversation(configuration: ConversationConfiguration): ActiveConversation {
        val agent = when (configuration.agent) {
            is QuestionAnswerAgentConfiguration -> buildQuestionAnswerAgent(configuration.agent)
            is RoleplayingAgentConfiguration -> buildRoleplayingAgent(configuration.agent)
        }

        val conversation = ActiveConversation(this)

        conversation.addAgent(agent)

        return conversation
    }
}

private fun buildKonvoConfiguration(configure: KonvoConfigurationBuilder.() -> Unit): KonvoConfiguration {
    class ConfigurationBuilder : KonvoConfigurationBuilder {
        override var dataDirectory: String = "./data"
        private val modelProviders = mutableListOf<ModelProvider>()
        private val promptProviders = mutableListOf<PromptProvider>()
        private val toolProviders = mutableListOf<ToolProvider>()

        override fun installModels(provider: ModelProvider) {
            modelProviders.add(provider)
        }

        override fun installPrompts(provider: PromptProvider) {
            promptProviders.add(provider)
        }

        override fun installTools(provider: ToolProvider) {
            toolProviders.add(provider)
        }

        fun build(): KonvoConfiguration {
            return KonvoConfiguration(
                dataDirectory = dataDirectory,
                modelProviders = modelProviders.toList(),
                promptProviders = promptProviders.toList(),
                toolProviders = toolProviders.toList(),
            )
        }
    }

    val scope = ConfigurationBuilder()
    scope.configure()
    val configuration = scope.build()
    return configuration
}
