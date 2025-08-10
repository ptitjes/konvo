package io.github.ptitjes.konvo.frontend.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import io.github.ptitjes.konvo.frontend.compose.viewmodels.*

/**
 * A screen that allows creating a new conversation.
 *
 * @param konvo The Konvo instance to use for creating the conversation
 * @param onConversationCreated Callback for when a conversation is created
 * @param modifier The modifier to apply to this component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewConversationScreen(
    konvo: Konvo,
    onConversationCreated: (ActiveConversation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = remember { NewConversationViewModel(konvo) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Conversation",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.createConversation(onConversationCreated)
                        },
                        enabled = viewModel.isCreateEnabled
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Create",
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AgentTypeSelector(
                selectedAgentType = viewModel.selectedAgentType,
                onAgentTypeSelected = { viewModel.onAgentTypeSelected(it) },
                agentTypes = AgentType.entries
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Agent Configuration Form
            when (viewModel.selectedAgentType) {
                AgentType.QuestionAnswer -> {
                    QuestionAnswerConfigurationForm(
                        prompts = viewModel.prompts,
                        tools = viewModel.tools,
                        models = viewModel.models,
                        selectedPrompt = viewModel.selectedPrompt,
                        onPromptSelected = { viewModel.onPromptSelected(it) },
                        selectedTools = viewModel.selectedTools,
                        onToolsSelected = { viewModel.onToolsSelected(it) },
                        selectedModel = viewModel.selectedQAModel,
                        onModelSelected = { viewModel.onQAModelSelected(it) }
                    )
                }

                AgentType.Roleplaying -> {
                    RoleplayingConfigurationForm(
                        characters = viewModel.characters,
                        models = viewModel.models,
                        selectedCharacter = viewModel.selectedCharacter,
                        onCharacterSelected = { viewModel.onCharacterSelected(it) },
                        selectedGreetingIndex = viewModel.selectedGreetingIndex,
                        onGreetingIndexSelected = { viewModel.onGreetingIndexSelected(it) },
                        userName = viewModel.userName,
                        onUserNameChanged = { viewModel.onUserNameChanged(it) },
                        selectedModel = viewModel.selectedRPModel,
                        onModelSelected = { viewModel.onRPModelSelected(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuestionAnswerConfigurationForm(
    prompts: List<PromptCard>,
    tools: List<ToolCard>,
    models: List<ModelCard>,
    selectedPrompt: PromptCard?,
    onPromptSelected: (PromptCard) -> Unit,
    selectedTools: List<ToolCard>,
    onToolsSelected: (List<ToolCard>) -> Unit,
    selectedModel: ModelCard,
    onModelSelected: (ModelCard) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (prompts.isNotEmpty() && selectedPrompt != null) {
            PromptSelector(
                selectedPrompt = selectedPrompt,
                onPromptSelected = onPromptSelected,
                prompts = prompts
            )
        }

        ToolSelector(
            selectedTools = selectedTools,
            onToolsSelected = onToolsSelected,
            tools = tools
        )

        ModelSelector(
            selectedModel = selectedModel,
            onModelSelected = onModelSelected,
            models = models
        )
    }
}

@Composable
private fun RoleplayingConfigurationForm(
    characters: List<CharacterCard>,
    models: List<ModelCard>,
    selectedCharacter: CharacterCard,
    onCharacterSelected: (CharacterCard) -> Unit,
    selectedGreetingIndex: Int?,
    onGreetingIndexSelected: (Int?) -> Unit,
    userName: String,
    onUserNameChanged: (String) -> Unit,
    selectedModel: ModelCard,
    onModelSelected: (ModelCard) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CharacterSelector(
            selectedCharacter = selectedCharacter,
            onCharacterSelected = { character ->
                onCharacterSelected(character)
                // Reset greeting index when the character changes
                onGreetingIndexSelected(null)
            },
            characters = characters
        )

        // Greeting Selection (only if the character has greetings)
        if (selectedCharacter.greetings.isNotEmpty()) {
            CharacterGreetingSelector(
                selectedGreetingIndex = selectedGreetingIndex,
                onGreetingIndexSelected = onGreetingIndexSelected,
                character = selectedCharacter
            )
        }

        OutlinedTextField(
            label = {
                Text(
                    text = "Your Persona",
                    style = MaterialTheme.typography.titleSmall
                )
            },
            value = userName,
            onValueChange = onUserNameChanged,
            modifier = Modifier.fillMaxWidth()
        )

        ModelSelector(
            selectedModel = selectedModel,
            onModelSelected = onModelSelected,
            models = models
        )
    }
}
