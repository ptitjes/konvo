package io.github.ptitjes.konvo.frontend.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.conversation.model.*
import io.github.ptitjes.konvo.core.models.*
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
                val selectedAgentType = viewModel.selectedAgentType
                val questionAnswer by viewModel.questionAnswer.collectAsState()
                val roleplay by viewModel.roleplay.collectAsState()

                NewConversationPanel(
                    selectedAgentType = selectedAgentType,
                    questionAnswer = questionAnswer,
                    roleplay = roleplay,
                    onSelectAgentType = viewModel::selectAgentType,
                    onSelectQuestionAnswerTools = viewModel::selectQuestionAnswerTools,
                    onSelectQuestionAnswerModel = viewModel::selectQuestionAnswerModel,
                    onSelectRoleplayCharacter = viewModel::selectRoleplayCharacter,
                    onSelectRoleplayGreetingIndex = viewModel::selectRoleplayGreetingIndex,
                    onChangeRoleplayUserName = viewModel::changeRoleplayUserName,
                    onSelectRoleplayModel = viewModel::selectRoleplayModel,
                )
            }
        }
    }
}

@Composable
private fun NewConversationPanel(
    selectedAgentType: AgentType,
    questionAnswer: NewQuestionAnswerState,
    roleplay: NewRoleplayState,
    onSelectAgentType: (AgentType) -> Unit,
    onSelectQuestionAnswerTools: (List<ToolCard>) -> Unit,
    onSelectQuestionAnswerModel: (Model) -> Unit,
    onSelectRoleplayCharacter: (CharacterCard) -> Unit,
    onSelectRoleplayGreetingIndex: (Int?) -> Unit,
    onChangeRoleplayUserName: (String) -> Unit,
    onSelectRoleplayModel: (Model) -> Unit,
) {
    AgentTypeSelector(
        selectedAgentType = selectedAgentType,
        onSelectAgentType = onSelectAgentType,
        agentTypes = AgentType.entries,
    )

    when (selectedAgentType) {
        AgentType.QuestionAnswer -> {
            QuestionAnswerConfigurationForm(
                questionAnswer = questionAnswer,
                onSelectQuestionAnswerTools = onSelectQuestionAnswerTools,
                onSelectQuestionAnswerModel = onSelectQuestionAnswerModel,
            )
        }

        AgentType.Roleplay -> {
            RoleplayConfigurationForm(
                roleplay = roleplay,
                onSelectRoleplayCharacter = onSelectRoleplayCharacter,
                onSelectRoleplayGreetingIndex = onSelectRoleplayGreetingIndex,
                onChangeRoleplayUserName = onChangeRoleplayUserName,
                onSelectRoleplayModel = onSelectRoleplayModel,
            )
        }
    }
}

@Composable
private fun QuestionAnswerConfigurationForm(
    questionAnswer: NewQuestionAnswerState,
    onSelectQuestionAnswerTools: (List<ToolCard>) -> Unit,
    onSelectQuestionAnswerModel: (Model) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (questionAnswer) {
            NewQuestionAnswerState.Loading -> {
                FullSizeProgressIndicator()
            }

            is NewQuestionAnswerState.Unavailable -> {
                Text(
                    text = "No available models",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }

            is NewQuestionAnswerState.Available -> {
                ToolSelector(
                    selectedTools = questionAnswer.selectedTools,
                    onToolsSelected = onSelectQuestionAnswerTools,
                    tools = questionAnswer.availableTools
                )

                if (questionAnswer.selectableModels.isEmpty()) {
                    Text(
                        text = "No available models with tool support",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(16.dp),
                    )
                } else {
                    ModelSelector(
                        selectedModel = questionAnswer.selectedModel,
                        onModelSelected = onSelectQuestionAnswerModel,
                        models = questionAnswer.selectableModels
                    )
                }
            }
        }
    }
}

@Composable
private fun RoleplayConfigurationForm(
    roleplay: NewRoleplayState,
    onSelectRoleplayCharacter: (CharacterCard) -> Unit,
    onSelectRoleplayGreetingIndex: (Int?) -> Unit,
    onChangeRoleplayUserName: (String) -> Unit,
    onSelectRoleplayModel: (Model) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        when (val roleplay = roleplay) {
            NewRoleplayState.Loading -> {
                FullSizeProgressIndicator()
            }

            is NewRoleplayState.Unavailable -> {
                Text(
                    text = "No available characters or models",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }

            is NewRoleplayState.Available -> {
                CharacterGridSelector(
                    modifier = Modifier.weight(1f),
                    selectedCharacter = roleplay.selectedCharacter,
                    onCharacterSelected = { character ->
                        onSelectRoleplayCharacter(character)
                        // Reset greeting index when the character changes
                        onSelectRoleplayGreetingIndex(null)
                    },
                    characters = roleplay.availableCharacters,
                )

                if (roleplay.selectedCharacter.greetings.size > 1) {
                    CharacterGreetingSelector(
                        selectedGreetingIndex = roleplay.selectedGreetingIndex,
                        onGreetingIndexSelected = onSelectRoleplayGreetingIndex,
                        character = roleplay.selectedCharacter,
                        userName = roleplay.userName,
                    )
                }

                OutlinedTextField(
                    label = {
                        Text(
                            text = "Your Persona",
                            style = MaterialTheme.typography.titleSmall
                        )
                    },
                    value = roleplay.userName,
                    onValueChange = onChangeRoleplayUserName,
                    modifier = Modifier.fillMaxWidth()
                )

                ModelSelector(
                    selectedModel = roleplay.selectedModel,
                    onModelSelected = onSelectRoleplayModel,
                    models = roleplay.availableModels
                )
            }
        }
    }
}
