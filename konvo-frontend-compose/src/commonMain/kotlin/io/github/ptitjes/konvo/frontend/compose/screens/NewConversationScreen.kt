package io.github.ptitjes.konvo.frontend.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
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

    val selectedAgentType = viewModel.selectedAgentType
    val questionAnswer by viewModel.questionAnswer.collectAsState()
    val roleplay by viewModel.roleplay.collectAsState()

    val canCreate = when (selectedAgentType) {
        AgentType.QuestionAnswer -> questionAnswer.canCreate
        AgentType.Roleplay -> roleplay.canCreate
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier =
                            if (paneType != ListDetailPaneType.OnePane) Modifier.padding(start = 16.dp)
                            else Modifier,
                        text = "New Conversation",
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
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.Chat,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            viewModel.createConversation(onConversationCreated)
                        },
                        enabled = canCreate,
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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                NewConversationPanel(
                    selectedAgentType = selectedAgentType,
                    questionAnswer = questionAnswer,
                    roleplay = roleplay,
                    onSelectAgentType = viewModel::selectAgentType,
                    onSelectQuestionAnswerMcpServerNames = viewModel::selectQuestionAnswerMcpServerNames,
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
private fun ColumnScope.NewConversationPanel(
    selectedAgentType: AgentType,
    questionAnswer: NewQuestionAnswerState,
    roleplay: NewRoleplayState,
    onSelectAgentType: (AgentType) -> Unit,
    onSelectQuestionAnswerMcpServerNames: (Set<String>) -> Unit,
    onSelectQuestionAnswerModel: (ModelCard) -> Unit,
    onSelectRoleplayCharacter: (CharacterCard) -> Unit,
    onSelectRoleplayGreetingIndex: (Int?) -> Unit,
    onChangeRoleplayUserName: (String) -> Unit,
    onSelectRoleplayModel: (ModelCard) -> Unit,
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
                onSelectQuestionAnswerMcpServerNames = onSelectQuestionAnswerMcpServerNames,
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
private fun ColumnScope.QuestionAnswerConfigurationForm(
    questionAnswer: NewQuestionAnswerState,
    onSelectQuestionAnswerMcpServerNames: (Set<String>) -> Unit,
    onSelectQuestionAnswerModel: (ModelCard) -> Unit,
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
            McpServerSelector(
                selectedServers = questionAnswer.selectedMcpServers,
                onServersSelected = onSelectQuestionAnswerMcpServerNames,
                servers = questionAnswer.availableMcpServers
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

@Composable
private fun ColumnScope.RoleplayConfigurationForm(
    roleplay: NewRoleplayState,
    onSelectRoleplayCharacter: (CharacterCard) -> Unit,
    onSelectRoleplayGreetingIndex: (Int?) -> Unit,
    onChangeRoleplayUserName: (String) -> Unit,
    onSelectRoleplayModel: (ModelCard) -> Unit,
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
