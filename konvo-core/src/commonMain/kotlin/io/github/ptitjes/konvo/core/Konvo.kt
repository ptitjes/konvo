package io.github.ptitjes.konvo.core

import io.github.ptitjes.konvo.core.ai.spi.ModelCard
import io.github.ptitjes.konvo.core.ai.spi.ModelProvider
import io.github.ptitjes.konvo.core.ai.spi.Tool
import io.github.ptitjes.konvo.core.ai.spi.ToolProvider
import io.github.ptitjes.konvo.core.conversation.*
import kotlinx.coroutines.*
import kotlinx.io.files.*
import kotlin.coroutines.*

interface KonvoConfigurationBuilder {
    var dataDirectory: String
    fun installModels(provider: ModelProvider)
    fun installTools(provider: ToolProvider)
}

data class KonvoConfiguration(
    val dataDirectory: String,
    val modelProviders: List<ModelProvider>,
    val toolProviders: List<ToolProvider>,
)

suspend fun startKonvo(configure: KonvoConfigurationBuilder.() -> Unit): Konvo = coroutineScope {
    class ConfigurationBuilder : KonvoConfigurationBuilder {
        override var dataDirectory: String = "./data"
        private val modelProviders = mutableListOf<ModelProvider>()
        private val toolProviders = mutableListOf<ToolProvider>()

        override fun installModels(provider: ModelProvider) {
            modelProviders.add(provider)
        }

        override fun installTools(provider: ToolProvider) {
            toolProviders.add(provider)
        }

        fun build(): KonvoConfiguration {
            return KonvoConfiguration(
                dataDirectory = dataDirectory,
                modelProviders = modelProviders.toList(),
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
    private val handler = CoroutineExceptionHandler { _, exception ->
        // TODO use a logger
        println("Konvo failed: $exception")
        exception.printStackTrace()
    }
    override val coroutineContext: CoroutineContext = coroutineScope.coroutineContext + handler

    private lateinit var _models: List<ModelCard>;
    private lateinit var _tools: List<Tool>;
    private lateinit var _characters: List<Character>;

    suspend fun init() {
        _models = configuration.modelProviders.flatMap { it.queryModels() }
        _tools = configuration.toolProviders.flatMap { it.queryTools() }
        _characters = loadCharacters()
    }

    private fun loadCharacters(): List<Character> {
        return Character.loadCharacters(Path(configuration.dataDirectory, "characters"))
    }

    val models: List<ModelCard> get() = _models
    val characters: List<Character> get() = _characters
    val tools: List<Tool> get() = _tools

    fun createConversation(configuration: ConversationConfiguration): Conversation = when (configuration.mode) {
        is QuestionAnswerModeConfiguration -> QuestionAnswerConversation(
            coroutineScope = this,
            configuration = configuration.mode,
        )

        is RoleplayingModeConfiguration -> RoleplayingConversation(
            coroutineScope = this,
            configuration = configuration.mode,
        )
    }
}
