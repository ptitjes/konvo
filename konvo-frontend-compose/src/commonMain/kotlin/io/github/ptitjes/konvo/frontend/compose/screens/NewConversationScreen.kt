package io.github.ptitjes.konvo.frontend.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import io.github.ptitjes.konvo.frontend.compose.util.*
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
    viewModel: NewConversationViewModel = viewModel(),
    onConversationCreated: (Conversation) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val paneType = LocalListDetailPaneType.current

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
                navigationIcon = {
                    if (paneType == ListDetailPaneType.OnePane) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    }
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
                .padding(paddingValues)
                .fillMaxSize()
                .widthIn(max = 800.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AgentTypeSelector(
                    selectedAgentType = viewModel.selectedAgentType,
                    onAgentTypeSelected = { viewModel.onAgentTypeSelected(it) },
                    agentTypes = AgentType.entries
                )

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

                    AgentType.Roleplay -> {
                        RoleplayConfigurationForm(
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
private fun RoleplayConfigurationForm(
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
        CharacterGridSelector(
            modifier = Modifier.weight(1f),
            selectedCharacter = selectedCharacter,
            onCharacterSelected = { character ->
                onCharacterSelected(character)
                // Reset greeting index when the character changes
                onGreetingIndexSelected(null)
            },
            characters = characters,
        )

        if (selectedCharacter.greetings.size > 1) {
            CharacterGreetingSelector(
                selectedGreetingIndex = selectedGreetingIndex,
                onGreetingIndexSelected = onGreetingIndexSelected,
                character = selectedCharacter,
                userName = userName,
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
