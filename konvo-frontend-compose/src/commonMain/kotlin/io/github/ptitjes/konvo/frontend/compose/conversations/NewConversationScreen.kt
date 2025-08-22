package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.frontend.compose.agents.*
import io.github.ptitjes.konvo.frontend.compose.mcp.*
import io.github.ptitjes.konvo.frontend.compose.models.*
import io.github.ptitjes.konvo.frontend.compose.roleplay.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*

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
                    onChangeRoleplayPersona = viewModel::changeRoleplayPersona,
                    onSelectRoleplayModel = viewModel::selectRoleplayModel,
                    onSelectRoleplayLorebook = viewModel::selectRoleplayLorebook,
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
    onChangeRoleplayPersona: (Persona) -> Unit,
    onSelectRoleplayModel: (ModelCard) -> Unit,
    onSelectRoleplayLorebook: (Lorebook?) -> Unit,
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
                onChangeRoleplayPersona = onChangeRoleplayPersona,
                onSelectRoleplayModel = onSelectRoleplayModel,
                onSelectRoleplayLorebook = onSelectRoleplayLorebook,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.RoleplayConfigurationForm(
    roleplay: NewRoleplayState,
    onSelectRoleplayCharacter: (CharacterCard) -> Unit,
    onSelectRoleplayGreetingIndex: (Int?) -> Unit,
    onChangeRoleplayPersona: (Persona) -> Unit,
    onSelectRoleplayModel: (ModelCard) -> Unit,
    onSelectRoleplayLorebook: (Lorebook?) -> Unit,
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
                    personaName = roleplay.selectedPersona.nickname,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var showLorebookSheet by remember { mutableStateOf(false) }

                val personas by rememberSetting(PersonaSettingsKey, emptyList()) { it.personas }

                Column(modifier = Modifier.weight(1f)) {
                    PersonaSelector(
                        selectedPersona = roleplay.selectedPersona,
                        onPersonaSelected = { persona ->
                            onChangeRoleplayPersona(persona)
                            val preferredLorebook =
                                roleplay.availableLorebooks.firstOrNull { it.id == persona.defaultLorebookId }
                            if (preferredLorebook != null) onSelectRoleplayLorebook(preferredLorebook)
                        },
                        personas = personas,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                FilledTonalIconButton(
                    modifier = Modifier.offset(y = 4.dp),
                    onClick = { showLorebookSheet = true },
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Persona Settings"
                    )
                }
                if (showLorebookSheet) {
                    ModalBottomSheet(
                        onDismissRequest = { showLorebookSheet = false },
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                        ) {
                            LorebookSelector(
                                label = "Additional Lorebook",
                                selectedLorebook = roleplay.selectedLorebook,
                                onLorebookSelected = { selected ->
                                    onSelectRoleplayLorebook(selected)
                                    showLorebookSheet = false
                                },
                                lorebooks = roleplay.availableLorebooks,
                            )
                        }
                    }
                }
            }

            ModelSelector(
                selectedModel = roleplay.selectedModel,
                onModelSelected = onSelectRoleplayModel,
                models = roleplay.availableModels
            )
        }
    }
}
