package io.github.ptitjes.konvo.core

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.*
import kotlinx.coroutines.*
import kotlinx.io.files.*
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

suspend fun startKonvo(configure: KonvoConfigurationBuilder.() -> Unit): Konvo = coroutineScope {
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

    val konvo = Konvo(this, scope.build())
    konvo.init()

    return@coroutineScope konvo
}

class Konvo(
    coroutineScope: CoroutineScope,
    private val configuration: KonvoConfiguration,
) : CoroutineScope {

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception caught in Konvo" }
    }
    override val coroutineContext: CoroutineContext = coroutineScope.coroutineContext + handler

    private lateinit var _models: List<ModelCard>;
    private lateinit var _prompts: List<PromptCard>;
    private lateinit var _tools: List<ToolCard>;
    private lateinit var _characters: List<Character>;

    suspend fun init() {
        _models = configuration.modelProviders.flatMap { it.queryModelCards() }
        _prompts = configuration.promptProviders.flatMap { it.queryPrompts() }
        _tools = configuration.toolProviders.flatMap { it.queryTools() }
        _characters = loadCharacters()
    }

    private fun loadCharacters(): List<Character> {
        return Character.loadCharacters(Path(configuration.dataDirectory, "characters"))
    }

    val models: List<ModelCard> get() = _models
    val characters: List<Character> get() = _characters
    val prompts: List<PromptCard> get() = _prompts
    val tools: List<ToolCard> get() = _tools

    suspend fun createConversation(configuration: ConversationConfiguration): Conversation {
        val agent = when (configuration.agent) {
            is QuestionAnswerAgentConfiguration -> buildQuestionAnswerAgent(configuration.agent)
            is RoleplayingAgentConfiguration -> buildRoleplayingAgent(configuration.agent)
        }

        val conversation = Conversation(this)

        conversation.addAgent(agent)

        return conversation
    }
}
