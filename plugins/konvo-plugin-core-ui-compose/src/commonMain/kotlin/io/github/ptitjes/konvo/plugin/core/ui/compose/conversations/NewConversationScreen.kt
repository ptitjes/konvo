package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.mcp.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.roleplay.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import org.jetbrains.compose.resources.*
import org.kodein.di.compose.*

@Composable
fun NewConversationScreen(
    viewModel: NewConversationViewModel = viewModel(),
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    NewConversationScreen(
        viewModel = viewModel,
        onConversationCreated = { navigator.navigateToConversation(it) },
        onProviderSettingsClick = { navigator.openSettingsSection("models") },
        onGoToSettingsClick = { navigator.openSettingsSection(it) },
        modifier = modifier,
    )
}

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
    onConversationCreated: (id: String) -> Unit,
    onProviderSettingsClick: () -> Unit,
    onGoToSettingsClick: (titleKey: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedAgentType = viewModel.selectedAgentType
    val questionAnswer by viewModel.questionAnswer.collectAsState()
    val roleplay by viewModel.roleplay.collectAsState()

    val canCreate = when (selectedAgentType) {
        AgentType.QuestionAnswer -> questionAnswer.canCreate
        AgentType.Roleplay -> roleplay.canCreate
    }

    val snackBarHostState = remember { SnackbarHostState() }

    val modelManager by rememberInstance<ModelManager>()

    LaunchedEffect(Unit) {
        modelManager.providersInError.collect { providers ->
            if (providers != null) {
                val providerNames = providers.joinToString(", ")
                val result = snackBarHostState.showSnackbar(
                    message = "Failed to load models from $providerNames",
                    actionLabel = "Settings",
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onProviderSettingsClick()
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        text = strings.conversations.newConversationTitle,
                    )
                },
                navigationIcon = {
                    LocalCenterStageControl.current.NavigationButton {
                        Icon(
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                            painter = painterResource(Res.drawable.ic_chat_bubble_outline),
                            contentDescription = strings.conversations.newConversationIconAria,
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
                            painter = painterResource(Res.drawable.ic_check),
                            contentDescription = strings.conversations.createAria,
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
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
                    onGoToSettingsClick = onGoToSettingsClick,
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
    onGoToSettingsClick: (titleKey: String) -> Unit,
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
                onGoToSettingsClick = onGoToSettingsClick,
                onSelectQuestionAnswerMcpServerNames = onSelectQuestionAnswerMcpServerNames,
                onSelectQuestionAnswerModel = onSelectQuestionAnswerModel,
            )
        }

        AgentType.Roleplay -> {
            RoleplayConfigurationForm(
                roleplay = roleplay,
                onGoToSettingsClick = onGoToSettingsClick,
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
    onGoToSettingsClick: (titleKey: String) -> Unit,
    onSelectQuestionAnswerMcpServerNames: (Set<String>) -> Unit,
    onSelectQuestionAnswerModel: (ModelCard) -> Unit,
) {
    when (questionAnswer) {
        NewQuestionAnswerState.Loading -> {
            FullSizeProgressIndicator()
        }

        is NewQuestionAnswerState.Available -> {
            McpServerSelector(
                selectedServers = questionAnswer.selectedMcpServers,
                onServersSelected = onSelectQuestionAnswerMcpServerNames,
                servers = questionAnswer.availableMcpServers
            )

            if (questionAnswer.selectableModels.isEmpty()) {
                UnavailabilityPlaceholder(
                    unavailabilityText = strings.conversations.qaNoModels,
                    onGoToSettings = { onGoToSettingsClick("models") },
                )
            } else {
                ModelSelector(
                    selectedModel = questionAnswer.selectedModel ?: questionAnswer.selectableModels.first(),
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
    onGoToSettingsClick: (titleKey: String) -> Unit,
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

        is NewRoleplayState.Available -> {
            if (roleplay.availableCharacters.isEmpty()) {
                UnavailabilityPlaceholder(
                    unavailabilityText = strings.conversations.rpNoAvailableCharacters,
                    onGoToSettings = { onGoToSettingsClick("characters") },
                )
            } else {
                val selectedCharacter = roleplay.selectedCharacter ?: roleplay.availableCharacters.first()

                CharacterGridSelector(
                    modifier = Modifier.weight(1f),
                    selectedCharacter = selectedCharacter,
                    onCharacterSelected = { character ->
                        onSelectRoleplayCharacter(character)
                        // Reset greeting index when the character changes
                        onSelectRoleplayGreetingIndex(null)
                    },
                    characters = roleplay.availableCharacters,
                )

                if (selectedCharacter.greetings.size > 1) {
                    CharacterGreetingSelector(
                        selectedGreetingIndex = roleplay.selectedGreetingIndex,
                        onGreetingIndexSelected = onSelectRoleplayGreetingIndex,
                        character = selectedCharacter,
                        personaName = roleplay.selectedPersona?.nickname ?: "<user>",
                    )
                }
            }

            if (roleplay.availablePersonas.isEmpty()) {
                UnavailabilityPlaceholder(
                    unavailabilityText = strings.conversations.rpNoAvailablePersonas,
                    onGoToSettings = { onGoToSettingsClick("personas") },
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    var showLorebookSheet by remember { mutableStateOf(false) }

                    val personas by rememberSetting(PersonaSettingsKey, emptyList()) { it.personas }

                    Column(modifier = Modifier.weight(1f)) {
                        PersonaSelector(
                            selectedPersona = roleplay.selectedPersona ?: roleplay.availablePersonas.first(),
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
                            painter = painterResource(Res.drawable.ic_settings),
                            contentDescription = strings.conversations.personaSettingsAria
                        )
                    }
                    if (showLorebookSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showLorebookSheet = false },
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                            ) {
                                if (roleplay.availableLorebooks.isEmpty()) {
                                    Text(
                                        text = strings.conversations.rpNoAvailableLorebooks,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(16.dp),
                                    )
                                } else {
                                    LorebookSelector(
                                        label = strings.conversations.additionalLorebookLabel,
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
                }
            }

            if (roleplay.availableModels.isEmpty()) {
                UnavailabilityPlaceholder(
                    unavailabilityText = strings.conversations.rpNoAvailableModel,
                    onGoToSettings = { onGoToSettingsClick("models") },
                )
            } else {
                ModelSelector(
                    selectedModel = roleplay.selectedModel ?: roleplay.availableModels.first(),
                    onModelSelected = onSelectRoleplayModel,
                    models = roleplay.availableModels
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun UnavailabilityPlaceholder(
    unavailabilityText: String,
    onGoToSettings: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.height(56.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = unavailabilityText,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(8.dp).weight(1f),
        )

        if (onGoToSettings != null) {
            TextButton(
                modifier = Modifier.height(32.dp),
                onClick = { onGoToSettings() },
                contentPadding =
                    PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 6.dp,
                        bottom = 6.dp,
                    ),
                shape = MaterialTheme.shapes.small,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val contentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

                    Icon(
                        modifier = Modifier.size(16.dp),
                        painter = painterResource(Res.drawable.ic_settings),
                        contentDescription = strings.settings.listTitle,
                        tint = contentColor,
                    )

                    Text(
                        text = strings.settings.listTitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor,
                    )
                }
            }
        }
    }
}
