package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.compose.runtime.*
import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.conversation.agents.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import kotlinx.coroutines.*

/**
 * ViewModel for the NewConversationScreen that encapsulates all the mutable state.
 */
class NewConversationViewModel(
    private val konvo: Konvo,
) : ViewModel() {
    val prompts get() = konvo.prompts
    val tools get() = konvo.tools
    val characters get() = konvo.characters
    val models
        get() = konvo.models.let { models ->
            if (selectedAgentType == AgentType.QuestionAnswer && selectedTools.isNotEmpty())
                models.filter { it.supportsTools }
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

    // Roleplaying configuration
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
            AgentType.Roleplaying -> userName.isNotBlank()
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

    fun createConversation(onConversationCreated: (ActiveConversation) -> Unit) {
        val configuration = when (selectedAgentType) {
            AgentType.QuestionAnswer -> {
                selectedPrompt?.let { prompt ->
                    selectedQAModel?.let { model ->
                        ConversationConfiguration(
                            agent = QuestionAnswerAgentConfiguration(
                                prompt = prompt,
                                tools = selectedTools,
                                model = model,
                            )
                        )
                    }
                }
            }

            AgentType.Roleplaying -> {
                selectedCharacter?.let { character ->
                    selectedRPModel?.let { model ->
                        ConversationConfiguration(
                            agent = RoleplayingAgentConfiguration(
                                character = character,
                                characterGreetingIndex = selectedGreetingIndex,
                                userName = userName,
                                model = model,
                            )
                        )
                    }
                }
            }
        }

        configuration?.let { config ->
            viewModelScope.launch {
                val conversation = konvo.createConversation(config)
                onConversationCreated(conversation)
            }
        }
    }
}
