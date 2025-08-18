package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.*
import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.tools.*
import io.github.ptitjes.konvo.core.util.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*
import kotlin.time.*

/**
 * ViewModel for the NewConversationScreen that encapsulates all the mutable state.
 */
class NewConversationViewModel(
    private val modelManager: ModelManager,
    private val characterManager: CharacterManager,
    private val mcpHostSessionFactory: (coroutineContext: CoroutineContext) -> McpHostSession,
    private val conversationRepository: ConversationRepository,
) : ViewModel() {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private lateinit var mcpHostSession: McpHostSession

    var selectedAgentType: AgentType by mutableStateOf(AgentType.QuestionAnswer)
        private set

    private val _questionAnswer = MutableStateFlow<NewQuestionAnswerState>(NewQuestionAnswerState.Loading)
    val questionAnswer = _questionAnswer.asStateFlow()

    private val _roleplay = MutableStateFlow<NewRoleplayState>(NewRoleplayState.Loading)
    val roleplay = _roleplay.asStateFlow()

    init {
        viewModelScope.launch {
            mcpHostSession = mcpHostSessionFactory(viewModelScope.coroutineContext)
            mcpHostSession.addAllServers()

            val availableModels = modelManager.models.first()
            val availableTools = mcpHostSession.tools.first()
            val availableCharacters = characterManager.characters.first()

            updateQuestionAnswerState(availableModels, availableTools)
            updateRoleplayState(availableModels, availableCharacters)

            launch {
                combine(
                    modelManager.models,
                    mcpHostSession.tools,
                    characterManager.characters
                ) { models, tools, characters ->
                    Triple(models, tools, characters)
                }.collect { (models, tools, characters) ->
                    logger.debug {
                        "Collecting ${models.size} models, ${tools.size} tools and ${characters.size} characters"
                    }
                    updateQuestionAnswerState(models, tools)
                    updateRoleplayState(models, characters)
                }
            }
        }
    }

    private fun updateQuestionAnswerState(
        availableModels: List<ModelCard>,
        availableTools: List<ToolCard>,
    ) {
        _questionAnswer.update { previous ->
            when {
                availableModels.isEmpty() -> NewQuestionAnswerState.Unavailable(
                    noAvailableModels = true,
                )

                previous is NewQuestionAnswerState.Available -> {
                    val noTools = previous.selectedTools.isEmpty()
                    val availableModels =
                        if (noTools) availableModels else availableModels.filter { it.supportsTools }

                    previous.copy(
                        availableModels = availableModels,
                        availableTools = availableTools,
                        selectedModel = availableModels.first(),
                        selectedTools = previous.selectedTools.intersect(availableTools.toSet()).toList(),
                    )
                }

                else -> NewQuestionAnswerState.Available(
                    availableModels = availableModels,
                    availableTools = availableTools,
                    selectedModel = availableModels.first(),
                    selectedTools = emptyList(),
                )
            }
        }
    }

    private fun updateRoleplayState(
        availableModels: List<ModelCard>,
        availableCharacters: List<CharacterCard>,
    ) {
        _roleplay.update { previous ->
            when {
                availableCharacters.isEmpty() || availableModels.isEmpty() -> NewRoleplayState.Unavailable(
                    noAvailableCharacters = availableModels.isEmpty(),
                    noAvailableModels = availableCharacters.isEmpty(),
                )

                previous is NewRoleplayState.Available -> {
                    previous.copy(
                        availableModels = availableModels,
                        selectedModel = availableModels.first(),
                    )
                }

                else -> NewRoleplayState.Available(
                    availableModels = availableModels,
                    availableCharacters = availableCharacters,
                    selectedCharacter = availableCharacters.first(),
                    selectedGreetingIndex = null,
                    userName = "User",
                    selectedModel = availableModels.first(),
                )
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

    fun selectQuestionAnswerTools(tools: List<ToolCard>) = updateQuestionAnswerState { state ->
        state.copy(
            selectedTools = tools,
            selectedModel =
                if (tools.isEmpty() || state.selectedModel.supportsTools) state.selectedModel
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

    val isCreateEnabled: Boolean
        get() {
            return when (selectedAgentType) {
                AgentType.QuestionAnswer -> questionAnswer.value.canCreate
                AgentType.Roleplay -> roleplay.value.canCreate
            }
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
        val availableTools: List<ToolCard>,
        val selectedModel: ModelCard,
        val selectedTools: List<ToolCard>,
    ) : NewQuestionAnswerState
}

val NewQuestionAnswerState.Available.selectableModels: List<ModelCard>
    get() =
        if (selectedTools.isEmpty()) availableModels
        else availableModels.filter { it.supportsTools }

val NewQuestionAnswerState.canCreate: Boolean
    get() = when (this) {
        is NewQuestionAnswerState.Available -> true
        else -> false
    }

fun NewQuestionAnswerState.createConfiguration(): QuestionAnswerAgentConfiguration =
    when (this) {
        is NewQuestionAnswerState.Available -> QuestionAnswerAgentConfiguration(
            toolNames = selectedTools.map { it.name },
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
            characterName = selectedCharacter.name,
            characterGreetingIndex = selectedGreetingIndex,
            userName = userName,
            modelName = selectedModel.name,
        )

        else -> error("Invalid state: $this")
    }
