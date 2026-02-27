package io.github.ptitjes.konvo.core.models

import io.github.ptitjes.konvo.core.models.providers.*
import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*
import kotlin.time.*

class SettingsBasedModelManager(
    coroutineContext: CoroutineContext,
    settingsRepository: SettingsRepository,
) : ModelManager {

    private val job = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + job)

    private val providers = settingsRepository.getSettings(ModelProviderSettingsKey)
        .map { it.providers }
        .stateIn(coroutineScope, SharingStarted.Eagerly, emptyList())

    private val _modelStatuses = MutableStateFlow<Map<String, ModelProviderStatus>>(emptyMap())
    val providerStatuses: StateFlow<Map<String, ModelProviderStatus>> get() = _modelStatuses.asStateFlow()

    private val reloadActionsChannel = Channel<ReloadAction>()

    init {
        coroutineScope.launch {
            launch {
                val previousDefinitions = mutableMapOf<String, ModelProviderConfiguration>()
                providers.collect { providers ->
                    previousDefinitions.keys.toList().forEach { name ->
                        if (providers.none { it.name == name }) {
                            previousDefinitions.remove(name)
                        }
                    }
                    providers.forEach { provider ->
                        if (provider.configuration != previousDefinitions[provider.name]) {
                            previousDefinitions[provider.name] = provider.configuration
                            doReloadModelProvider(provider)
                        }
                    }
                }
            }

            val reloadActions = reloadActionsChannel.consumeAsFlow()

            reloadActions.collect { reloadAction ->
                when (reloadAction) {
                    is ReloadAction.ReloadAll -> {
                        providers.value.forEach { provider -> doReloadModelProvider(provider) }
                    }

                    is ReloadAction.ReloadOne -> {
                        val provider = providers.value.first { it.name == reloadAction.providerName }
                        doReloadModelProvider(provider)
                    }
                }
            }
        }
    }

    private fun CoroutineScope.doReloadModelProvider(provider: NamedModelProvider) {
        launch {
            setPendingStatus(provider.name)
            runCatching { provider.loadModels() }
                .onSuccess { models -> setAvailableStatus(provider.name, models) }
                .onFailure { throwable -> setUnavailableStatus(provider.name, throwable) }
        }
    }

    private fun setPendingStatus(providerName: String) {
        updateStatus(providerName, ModelProviderStatus.Pending)
    }

    private fun setAvailableStatus(providerName: String, models: List<ModelCard>) {
        updateStatus(providerName, ModelProviderStatus.Available(models, Clock.System.now()))
    }

    private fun setUnavailableStatus(providerName: String, throwable: Throwable) {
        updateStatus(providerName, ModelProviderStatus.Unavailable(throwable.message ?: "Unknown error"))
    }

    private fun updateStatus(providerName: String, status: ModelProviderStatus) {
        _modelStatuses.update { statuses -> statuses + (providerName to status) }
    }

    fun reloadModelProvider(providerName: String) {
        reloadActionsChannel.trySend(ReloadAction.ReloadOne(providerName))
    }

    override val models: Flow<List<ModelCard>> =
        combine(providers, providerStatuses) { providers, statuses ->
            providers.flatMap { provider ->
                val status = statuses[provider.name]
                if (status == null || status is ModelProviderStatus.Pending) {
                    return@combine null
                }
                val available = status as? ModelProviderStatus.Available
                available?.models ?: emptyList()
            }
        }
            .filterNotNull()
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)

    override val providersInError: Flow<List<String>?> =
        providerStatuses.map { providerStatuses ->
            providerStatuses.mapNotNull { (name, status) ->
                if (status is ModelProviderStatus.Unavailable) name else null
            }.takeIf { it.isNotEmpty() }
        }
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
}

private sealed interface ReloadAction {
    data class ReloadOne(val providerName: String) : ReloadAction
    data object ReloadAll : ReloadAction
}

sealed interface ModelProviderStatus {
    data object Pending : ModelProviderStatus
    data class Available(
        val models: List<ModelCard>,
        val queryTime: Instant,
    ) : ModelProviderStatus

    data class Unavailable(val reason: String) : ModelProviderStatus
}

suspend fun NamedModelProvider.test() = runCatching { buildModelProvider().queryModels() }

private suspend fun NamedModelProvider.loadModels(): List<ModelCard> = withContext(Dispatchers.IO) {
    buildModelProvider().queryModels()
}

private fun NamedModelProvider.buildModelProvider(): ModelProvider = when (configuration) {
    is ModelProviderConfiguration.Anthropic -> AnthropicModelProvider(
        name = name,
        apiKey = configuration.apiKey,
    )

    is ModelProviderConfiguration.Ollama -> OllamaModelProvider(
        name = name,
        baseUrl = configuration.url,
    )

    is ModelProviderConfiguration.OpenAI -> OpenAIModelProvider(
        name = name,
        apiKey = configuration.apiKey,
    )

    is ModelProviderConfiguration.Google -> GoogleModelProvider(
        name = name,
        apiKey = configuration.apiKey,
    )

    is ModelProviderConfiguration.MistralAI -> MistralAIModelProvider(
        name = name,
        apiKey = configuration.apiKey,
    )
}
