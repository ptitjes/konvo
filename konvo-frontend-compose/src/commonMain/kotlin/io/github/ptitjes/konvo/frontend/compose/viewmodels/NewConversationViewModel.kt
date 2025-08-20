package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.util.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

/**
 * ViewModel for the NewConversationScreen that encapsulates all the mutable state.
 */
class NewConversationViewModel(
    private val modelManager: ModelManager,
    private val characterManager: CharacterManager,
    mcpServerSpecificationsManager: McpServerSpecificationsManager,
    private val conversationRepository: ConversationRepository,
    settingsRepository: io.github.ptitjes.konvo.core.settings.SettingsRepository,
) : ViewModel() {

    private val mcpServerNames = mcpServerSpecificationsManager.specifications.map { it.keys }
    private val roleplaySettings = settingsRepository.getSettings(RoleplayAgentSettingsKey)

    var selectedAgentType: AgentType by mutableStateOf(AgentType.QuestionAnswer)
        private set

    private val _questionAnswer = MutableStateFlow<NewQuestionAnswerState>(NewQuestionAnswerState.Loading)
    val questionAnswer = _questionAnswer.asStateFlow()

    private val _roleplay = MutableStateFlow<NewRoleplayState>(NewRoleplayState.Loading)
    val roleplay = _roleplay.asStateFlow()

    init {
        viewModelScope.launch {
            val availableModels = modelManager.models.first()
            val availableMcpServerNames = mcpServerNames.first()
            val availableCharacters = characterManager.characters.first()
            val roleplaySettings = roleplaySettings.first()

            updateQuestionAnswerState(availableModels, availableMcpServerNames)
            updateRoleplayState(availableModels, availableCharacters, roleplaySettings)

            data class ObservedData(
                val models: List<ModelCard>,
                val mcpServerNames: Set<String>,
                val characters: List<CharacterCard>,
            )

            launch {
                combine(
                    modelManager.models,
                    mcpServerNames,
                    characterManager.characters,
                    transform = ::ObservedData,
                ).collect { data ->
                    updateQuestionAnswerState(data.models, data.mcpServerNames)
                    updateRoleplayState(data.models, data.characters, roleplaySettings)
                }
            }
        }
    }

    private fun updateQuestionAnswerState(
        availableModels: List<ModelCard>,
        availableMcpServerNames: Set<String>,
    ) {
        _questionAnswer.update { previous ->
            when {
                availableModels.isEmpty() -> NewQuestionAnswerState.Unavailable(
                    noAvailableModels = true,
                )

                previous is NewQuestionAnswerState.Available -> {
                    previous.copy(
                        availableModels = availableModels,
                        availableMcpServers = availableMcpServerNames,
                        selectedModel = previous.selectedModel,
                        selectedMcpServers = previous.selectedMcpServers,
                    )
                }

                else -> NewQuestionAnswerState.Available(
                    availableModels = availableModels,
                    availableMcpServers = availableMcpServerNames,
                    selectedModel = availableModels.first(),
                    selectedMcpServers = emptySet(),
                )
            }
        }
    }

    private fun updateRoleplayState(
        availableModels: List<ModelCard>,
        availableCharacters: List<CharacterCard>,
        roleplaySettings: RoleplayAgentSettings,
    ) {
        _roleplay.update { previous ->
            when {
                availableCharacters.isEmpty() || availableModels.isEmpty() -> NewRoleplayState.Unavailable(
                    noAvailableCharacters = availableModels.isEmpty(),
                    noAvailableModels = availableCharacters.isEmpty(),
                )

                previous is NewRoleplayState.Available -> {
                    val previouslySelectedModel = availableModels.firstOrNull {
                        it.name == previous.selectedModel.name
                    }

                    previous.copy(
                        availableModels = availableModels,
                        selectedModel = previouslySelectedModel ?: availableModels.first(),
                    )
                }

                else -> {
                    val preferredModel = roleplaySettings.defaultPreferredModelName?.let { name ->
                        availableModels.firstOrNull { it.name == name }
                    }

                    NewRoleplayState.Available(
                        availableModels = availableModels,
                        availableCharacters = availableCharacters,
                        selectedCharacter = availableCharacters.first(),
                        selectedGreetingIndex = null,
                        userName = roleplaySettings.defaultUserPersonaName,
                        selectedModel = preferredModel ?: availableModels.first(),
                    )
                }
            }
        }
    }

    fun selectAgentType(agentType: AgentType) {
        selectedAgentType = agentType
    }

    private fun updateQuestionAnswerState(
        updater: (previous: NewQuestionAnswerState.Available) -> NewQuestionAnswerState,
    ) {
        _questionAnswer.update { currentState ->
            check(currentState is NewQuestionAnswerState.Available)
            updater(currentState)
        }
    }

    private fun updateRoleplayState(
        updater: (previous: NewRoleplayState.Available) -> NewRoleplayState,
    ) {
        _roleplay.update { currentState ->
            check(currentState is NewRoleplayState.Available)
            updater(currentState)
        }
    }

    fun selectQuestionAnswerMcpServerNames(mcpServerNames: Set<String>) = updateQuestionAnswerState { state ->
        state.copy(
            selectedMcpServers = mcpServerNames,
            selectedModel =
                if (mcpServerNames.isEmpty() || state.selectedModel.supportsTools) state.selectedModel
                else state.availableModels.first { it.supportsTools },
        )
    }

    fun selectQuestionAnswerModel(model: ModelCard) = updateQuestionAnswerState {
        it.copy(selectedModel = model)
    }

    fun selectRoleplayCharacter(character: CharacterCard) = updateRoleplayState {
        it.copy(
            selectedCharacter = character,
            selectedGreetingIndex = null,
        )
    }

    fun selectRoleplayGreetingIndex(index: Int?) = updateRoleplayState {
        it.copy(selectedGreetingIndex = index)
    }

    fun changeRoleplayUserName(userName: String) = updateRoleplayState {
        it.copy(userName = userName)
    }

    fun selectRoleplayModel(model: ModelCard) = updateRoleplayState {
        it.copy(selectedModel = model)
    }

    @OptIn(ExperimentalTime::class)
    fun createConversation(onConversationCreated: (Conversation) -> Unit) = viewModelScope.launch {
        val agentConfiguration = createAgentConfiguration()

        val now = SystemTimeProvider.now()

        val conversation = Conversation(
            id = UuidIdGenerator.newId(),
            title = "Untitled conversation",
            createdAt = now,
            updatedAt = now,
            participants = listOf(),
            lastMessagePreview = null,
            messageCount = 0,
            agentConfiguration = agentConfiguration,
        )

        conversationRepository.createConversation(conversation)

        onConversationCreated(conversation)
    }

    private fun createAgentConfiguration(): AgentConfiguration {
        return when (selectedAgentType) {
            AgentType.QuestionAnswer -> questionAnswer.value.createConfiguration()
            AgentType.Roleplay -> roleplay.value.createConfiguration()
        }
    }
}

sealed interface NewQuestionAnswerState {
    data object Loading : NewQuestionAnswerState
    data class Unavailable(
        val noAvailableModels: Boolean,
    ) : NewQuestionAnswerState

    data class Available(
        val availableModels: List<ModelCard>,
        val availableMcpServers: Set<String>,
        val selectedModel: ModelCard,
        val selectedMcpServers: Set<String>,
    ) : NewQuestionAnswerState
}

val NewQuestionAnswerState.Available.selectableModels: List<ModelCard>
    get() =
        if (selectedMcpServers.isEmpty()) availableModels
        else availableModels.filter { it.supportsTools }

val NewQuestionAnswerState.canCreate: Boolean
    get() = when (this) {
        is NewQuestionAnswerState.Available -> true
        else -> false
    }

fun NewQuestionAnswerState.createConfiguration(): QuestionAnswerAgentConfiguration =
    when (this) {
        is NewQuestionAnswerState.Available -> QuestionAnswerAgentConfiguration(
            mcpServerNames = selectedMcpServers,
            modelName = selectedModel.name,
        )

        else -> error("Invalid state: $this")
    }

sealed interface NewRoleplayState {
    data object Loading : NewRoleplayState
    data class Unavailable(
        val noAvailableCharacters: Boolean,
        val noAvailableModels: Boolean,
    ) : NewRoleplayState

    data class Available(
        val availableModels: List<ModelCard>,
        val availableCharacters: List<CharacterCard>,
        val selectedCharacter: CharacterCard,
        val selectedGreetingIndex: Int?,
        val userName: String,
        val selectedModel: ModelCard,
    ) : NewRoleplayState
}

val NewRoleplayState.canCreate: Boolean
    get() = when (this) {
        is NewRoleplayState.Available -> userName.isNotBlank()
        else -> false
    }

fun NewRoleplayState.createConfiguration(): RoleplayAgentConfiguration =
    when (this) {
        is NewRoleplayState.Available -> RoleplayAgentConfiguration(
            characterId = selectedCharacter.id,
            characterGreetingIndex = selectedGreetingIndex,
            userName = userName,
            modelName = selectedModel.name,
        )

        else -> error("Invalid state: $this")
    }
