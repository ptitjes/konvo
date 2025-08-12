package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.agents.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import io.github.ptitjes.konvo.core.util.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import kotlinx.coroutines.*
import kotlin.time.*

/**
 * ViewModel for the NewConversationScreen that encapsulates all the mutable state.
 */
class NewConversationViewModel(
    private val konvo: Konvo,
    private val conversationRepository: ConversationRepository,
) : ViewModel() {
    val prompts get() = konvo.prompts
    val tools get() = konvo.tools
    val characters get() = konvo.characters
    val models
        get() = konvo.models.let { models ->
            if (selectedAgentType == AgentType.QuestionAnswer && selectedTools.isNotEmpty()) models.filter { it.supportsTools }
            else models
        }

    // Agent type selection
    var selectedAgentType by mutableStateOf(AgentType.QuestionAnswer)
        private set

    // Question Answer configuration
    var selectedPrompt by mutableStateOf(prompts.first())
        private set
    var selectedTools by mutableStateOf<List<ToolCard>>(emptyList())
        private set
    var selectedQAModel by mutableStateOf(models.first())
        private set

    // Role-play configuration
    var selectedCharacter by mutableStateOf(characters.first())
        private set
    var selectedGreetingIndex by mutableStateOf<Int?>(null)
        private set
    var userName by mutableStateOf("User")
        private set
    var selectedRPModel by mutableStateOf(models.first())
        private set

    // Computed properties
    val isCreateEnabled: Boolean
        get() = when (selectedAgentType) {
            AgentType.QuestionAnswer -> true
            AgentType.Roleplay -> userName.isNotBlank()
        }

    // Event handlers
    fun onAgentTypeSelected(agentType: AgentType) {
        selectedAgentType = agentType
    }

    fun onPromptSelected(prompt: PromptCard) {
        selectedPrompt = prompt
    }

    fun onToolsSelected(tools: List<ToolCard>) {
        selectedTools = tools
        if (selectedTools.isNotEmpty() && !selectedQAModel.supportsTools) {
            selectedQAModel = models.first { it.supportsTools }
        }
    }

    fun onQAModelSelected(model: ModelCard) {
        selectedQAModel = model
    }

    fun onCharacterSelected(character: CharacterCard) {
        selectedCharacter = character
        // Reset greeting index when the character changes
        selectedGreetingIndex = null
    }

    fun onGreetingIndexSelected(index: Int?) {
        selectedGreetingIndex = index
    }

    fun onUserNameChanged(name: String) {
        userName = name
    }

    fun onRPModelSelected(model: ModelCard) {
        selectedRPModel = model
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

    private fun createAgentConfiguration(): AgentConfiguration = when (selectedAgentType) {
        AgentType.QuestionAnswer -> {
            QuestionAnswerAgentConfiguration(
                prompt = selectedPrompt,
                tools = selectedTools,
                model = selectedQAModel,
            )
        }

        AgentType.Roleplay -> {
            RoleplayAgentConfiguration(
                character = selectedCharacter,
                characterGreetingIndex = selectedGreetingIndex,
                userName = userName,
                model = selectedRPModel,
            )
        }
    }
}
