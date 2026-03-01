package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.runtime.*
import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.settings.*
import io.github.ptitjes.konvo.frontend.compose.agents.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * ViewModel for the NewConversationScreen that encapsulates all the mutable state.
 */
class NewConversationViewModel(
    private val modelManager: ModelManager,
    private val characterManager: CharacterManager,
    private val lorebookManager: LorebookManager,
    mcpServerSpecificationsManager: McpServerSpecificationsManager,
    private val conversationManager: ConversationManager,
    settingsRepository: SettingsRepository,
) : ViewModel() {

    private val mcpServerNames = mcpServerSpecificationsManager.specifications.map { it.keys }
    private val roleplaySettings = settingsRepository.getSettings(RoleplaySettingsKey)
    private val personaSettings = settingsRepository.getSettings(PersonaSettingsKey).map { it.personas }

    var selectedAgentType: AgentType by mutableStateOf(AgentType.QuestionAnswer)
        private set

    private val _questionAnswer = MutableStateFlow<NewQuestionAnswerState>(NewQuestionAnswerState.Loading)
    val questionAnswer = _questionAnswer.asStateFlow()

    private val _roleplay = MutableStateFlow<NewRoleplayState>(NewRoleplayState.Loading)
    val roleplay = _roleplay.asStateFlow()

    init {
        println("Initializing NewConversationViewModel")
        viewModelScope.launch {
            val availableModels = modelManager.models.first()
            val availableMcpServerNames = mcpServerNames.first()
            val availableCharacters = characterManager.characters.first()
            val availablePersonas = personaSettings.first()
            val availableLorebooks = lorebookManager.lorebooks.first()
            val roleplaySettings = roleplaySettings.first()

            updateQuestionAnswerState(
                availableModels = availableModels,
                availableMcpServerNames = availableMcpServerNames,
            )
            updateRoleplayState(
                availableModels = availableModels,
                availableCharacters = availableCharacters,
                availablePersonas = availablePersonas,
                availableLorebooks = availableLorebooks,
                roleplaySettings = roleplaySettings,
            )

            data class ObservedData(
                val models: List<ModelCard>,
                val mcpServerNames: Set<String>,
                val characters: List<CharacterCard>,
                val personas: List<Persona>,
                val lorebooks: List<Lorebook>,
            )

            launch {
                combine(
                    modelManager.models,
                    mcpServerNames,
                    characterManager.characters,
                    personaSettings,
                    lorebookManager.lorebooks,
                    transform = ::ObservedData,
                ).collect { data ->
                    updateQuestionAnswerState(
                        availableModels = data.models,
                        availableMcpServerNames = data.mcpServerNames,
                    )
                    updateRoleplayState(
                        availableModels = data.models,
                        availableCharacters = data.characters,
                        availablePersonas = availablePersonas,
                        availableLorebooks = data.lorebooks,
                        roleplaySettings = roleplaySettings,
                    )
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        println("Cleared NewConversationViewModel")
    }

    private fun updateQuestionAnswerState(
        availableModels: List<ModelCard>,
        availableMcpServerNames: Set<String>,
    ) {
        val sortedModels by lazy { availableModels.sortedBy { it.name } }

        _questionAnswer.update { previous ->
            when {
                previous is NewQuestionAnswerState.Available -> {
                    previous.copy(
                        availableModels = sortedModels,
                        availableMcpServers = availableMcpServerNames,
                        selectedModel = previous.selectedModel,
                        selectedMcpServers = previous.selectedMcpServers,
                    )
                }

                else -> NewQuestionAnswerState.Available(
                    availableModels = sortedModels,
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
        availablePersonas: List<Persona>,
        availableLorebooks: List<Lorebook>,
        roleplaySettings: RoleplaySettings,
    ) {
        _roleplay.update { previous ->
            when {
                previous is NewRoleplayState.Available -> {
//                    val previouslySelectedModel = previous.selectedModel?.name?.let { modelName ->
//                        availableModels.firstOrNull { it.name == modelName }
//                    }
//                    val previouslySelectedCharacter = previous.selectedCharacter?.id?.let { characterId ->
//                        availableCharacters.firstOrNull { it.id == characterId }
//                    }
//                    val previouslySelectedPersona = previous.selectedPersona?.name?.let { personaName ->
//                        availablePersonas.firstOrNull { it.name == personaName }
//                    }
//                    val previouslySelectedLorebook = previous.selectedLorebook?.id?.let { lorebookId ->
//                        availableLorebooks.firstOrNull { it.id == lorebookId }
//                    }

                    previous.copy(
                        availableModels = availableModels,
                        availableCharacters = availableCharacters,
                        availablePersonas = availablePersonas,
                        availableLorebooks = availableLorebooks,
                        selectedModel = previous.selectedModel ?: availableModels.firstOrNull(),
                        selectedCharacter = previous.selectedCharacter ?: availableCharacters.firstOrNull(),
                        selectedPersona = previous.selectedPersona ?: availablePersonas.firstOrNull(),
                        selectedLorebook = previous.selectedLorebook,
                    )
                }

                else -> {
                    val preferredModel = roleplaySettings.defaultPreferredModelName?.let { name ->
                        availableModels.firstOrNull { it.name == name }
                    }
                    val preferredPersona =
                        availablePersonas.firstOrNull { it.name == roleplaySettings.defaultPersonaName }

                    NewRoleplayState.Available(
                        availableModels = availableModels,
                        availableCharacters = availableCharacters,
                        availablePersonas = availablePersonas,
                        availableLorebooks = availableLorebooks,
                        selectedModel = preferredModel ?: availableModels.firstOrNull(),
                        selectedCharacter = availableCharacters.firstOrNull(),
                        selectedGreetingIndex = null,
                        selectedPersona = preferredPersona ?: availablePersonas.firstOrNull(),
                        selectedLorebook = null,
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
                if (mcpServerNames.isEmpty() || state.selectedModel?.supportsTools ?: false) state.selectedModel
                else state.availableModels.firstOrNull { it.supportsTools },
        )
    }

    fun selectQuestionAnswerModel(model: ModelCard) = updateQuestionAnswerState {
        it.copy(selectedModel = model)
    }

    fun selectRoleplayCharacter(character: CharacterCard) = updateRoleplayState {
        it.copy(
            selectedCharacter = character,
            selectedGreetingIndex = if (character.id != it.selectedCharacter?.id) null else it.selectedGreetingIndex,
        )
    }

    fun selectRoleplayGreetingIndex(index: Int?) = updateRoleplayState {
        it.copy(selectedGreetingIndex = index)
    }

    fun changeRoleplayPersona(persona: Persona) = updateRoleplayState {
        it.copy(selectedPersona = persona)
    }

    fun selectRoleplayModel(model: ModelCard) = updateRoleplayState {
        it.copy(selectedModel = model)
    }

    fun selectRoleplayLorebook(lorebook: Lorebook?) = updateRoleplayState {
        it.copy(selectedLorebook = lorebook)
    }

    fun createConversation(onConversationCreated: (id: String) -> Unit) {
        viewModelScope.launch {
            val agentConfiguration = createAgentConfiguration()

            val conversation = conversationManager.newConversation(
                agentConfiguration = agentConfiguration,
            )

            onConversationCreated(conversation.id)
        }
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

    data class Available(
        val availableModels: List<ModelCard>,
        val availableMcpServers: Set<String>,
        val selectedModel: ModelCard?,
        val selectedMcpServers: Set<String>,
    ) : NewQuestionAnswerState
}

val NewQuestionAnswerState.Available.selectableModels: List<ModelCard>
    get() =
        if (selectedMcpServers.isEmpty()) availableModels
        else availableModels.filter { it.supportsTools }

val NewQuestionAnswerState.canCreate: Boolean
    get() = when (this) {
        is NewQuestionAnswerState.Available -> selectedModel != null
        else -> false
    }

fun NewQuestionAnswerState.createConfiguration(): QuestionAnswerAgentConfiguration =
    when (this) {
        is NewQuestionAnswerState.Available if (selectedModel != null) -> QuestionAnswerAgentConfiguration(
            mcpServerNames = selectedMcpServers,
            modelName = selectedModel.name,
        )

        else -> error("Invalid state: $this")
    }

sealed interface NewRoleplayState {
    data object Loading : NewRoleplayState

    data class Available(
        val availableModels: List<ModelCard>,
        val availableCharacters: List<CharacterCard>,
        val availablePersonas: List<Persona>,
        val availableLorebooks: List<Lorebook>,
        val selectedCharacter: CharacterCard?,
        val selectedGreetingIndex: Int?,
        val selectedLorebook: Lorebook?,
        val selectedPersona: Persona?,
        val selectedModel: ModelCard?,
    ) : NewRoleplayState
}

val NewRoleplayState.canCreate: Boolean
    get() = when (this) {
        is NewRoleplayState.Available -> selectedCharacter != null && selectedModel != null && selectedPersona != null
        else -> false
    }

fun NewRoleplayState.createConfiguration(): RoleplayAgentConfiguration =
    when (this) {
        is NewRoleplayState.Available if (selectedCharacter != null && selectedModel != null && selectedPersona != null) ->
            RoleplayAgentConfiguration(
                characterId = selectedCharacter.id,
                characterGreetingIndex = selectedGreetingIndex,
                personaName = selectedPersona.name,
                modelName = selectedModel.name,
                lorebookId = selectedLorebook?.id,
            )

        else -> error("Invalid state: $this")
    }
