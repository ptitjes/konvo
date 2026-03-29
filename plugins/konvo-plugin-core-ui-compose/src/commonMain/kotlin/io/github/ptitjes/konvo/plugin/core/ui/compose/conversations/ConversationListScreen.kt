package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.utils.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import org.jetbrains.compose.resources.*

@Serializable
data object ConversationListScreen : NavScreen {
    override fun toString(): String = "conversations"

    internal sealed interface State : CircuitUiState {
        data object Loading : State
        data class Error(val error: String) : State
        data class Loaded(
            val conversations: List<ConversationDigest>,
            val selectedConversationId: String?,
            val eventSink: (Event) -> Unit,
        ) : State
    }

    internal sealed interface Event {
        data object OpenNewConversation : Event
        data class SelectConversation(val id: String) : Event
        data class DeleteConversation(val id: String) : Event
        data object GoToSettings : Event
    }
}

internal class ConversationListPresenter(
    private val navigator: ConversationNavigator,
    private val repository: ConversationRepository,
) : Presenter<ConversationListScreen.State> {
    @Composable
    override fun present(): ConversationListScreen.State {
        var error by remember { mutableStateOf<String?>(null) }
        val conversations by repository.getDigests(sort = Sort.UpdatedDesc)
            .catch { e -> error = e.message ?: "Failed to load conversations" }
            .collectAsState(null)
        var selectedConversationId by remember { mutableStateOf(navigator.selectedConversationId) }

        LaunchedEffect(Unit) {
            snapshotFlow { navigator.selectedConversationId }
                .collect { selectedConversationId = it }
        }

        return state(
            error = error,
            conversations = conversations,
            selectedConversationId = selectedConversationId,
        )
    }

    @Composable
    private fun state(
        error: String?,
        conversations: List<ConversationDigest>?,
        selectedConversationId: String?,
    ): ConversationListScreen.State {
        // TODO make the repository use its own scope
        val coroutineScope = rememberCoroutineScope()

        return when {
            error != null -> ConversationListScreen.State.Error(error)
            conversations == null -> ConversationListScreen.State.Loading
            else -> ConversationListScreen.State.Loaded(
                conversations = conversations,
                selectedConversationId = selectedConversationId,
            ) { event ->
                when (event) {
                    is ConversationListScreen.Event.OpenNewConversation -> {
                        navigator.goToNewConversation()
                    }

                    is ConversationListScreen.Event.SelectConversation -> {
                        navigator.goToConversation(event.id)
                    }

                    is ConversationListScreen.Event.DeleteConversation -> {
                        coroutineScope.launch { repository.delete(event.id) }
                        if (navigator.selectedConversationId == event.id) {
                            navigator.goToNewConversation()
                        }
                    }

                    is ConversationListScreen.Event.GoToSettings -> {
                        navigator.openSettings()
                    }
                }
            }
        }
    }
}

@Composable
internal fun ConversationList(
    state: ConversationListScreen.State,
    modifier: Modifier = Modifier,
) {
    ConversationListScreen(
        modifier = modifier,
        isLoading = state is ConversationListScreen.State.Loading,
        error = (state as? ConversationListScreen.State.Error)?.error,
        conversations = (state as? ConversationListScreen.State.Loaded)?.conversations ?: emptyList(),
        selectedConversationId = (state as? ConversationListScreen.State.Loaded)?.selectedConversationId,
        onCreateConversation = {
            state.ensureLoaded().eventSink(ConversationListScreen.Event.OpenNewConversation)
        },
        onSelectConversation = {
            state.ensureLoaded().eventSink(ConversationListScreen.Event.SelectConversation(it))
        },
        onDeleteConversation = {
            state.ensureLoaded().eventSink(ConversationListScreen.Event.DeleteConversation(it))
        },
        onSettingsClick = {
            state.ensureLoaded().eventSink(ConversationListScreen.Event.GoToSettings)
        },
    )
}

private fun ConversationListScreen.State.ensureLoaded(): ConversationListScreen.State.Loaded {
    return (this as ConversationListScreen.State.Loaded)
}

@Composable
private fun ConversationListScreen(
    isLoading: Boolean,
    error: String?,
    modifier: Modifier,
    conversations: List<ConversationDigest>,
    selectedConversationId: String?,
    onCreateConversation: () -> Unit,
    onSelectConversation: (String) -> Unit,
    onDeleteConversation: (String) -> Unit,
    onSettingsClick: () -> Unit,
) {
    val stageControl = LocalCenterStageControl.current
    val expanded = stageControl.navigationExpanded

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(message = error)
        }
    }

    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = MaterialTheme.colorScheme.onSurface

    val clickableModifier = if (!expanded) {
        val interactionSource = remember { MutableInteractionSource() }
        Modifier.clickable(interactionSource = interactionSource, indication = null) {
            stageControl.toggleNavigation()
        }
    } else Modifier

    val transition = updateTransition(expanded)

    Scaffold(
        modifier = modifier.then(clickableModifier),
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = contentColor,
                ),
                navigationIcon = { stageControl.NavigationButton() },
                title = {
                    Box {
                        transition.AnimatedVisibility(
                            modifier = Modifier.wrapContentSize(unbounded = true),
                            visible = { it },
                            enter = fadeIn(
                                animationSpec = tween(durationMillis = 100, delayMillis = 50),
                            ) + expandIn(
                                expandFrom = Alignment.CenterStart,
                                animationSpec = tween(durationMillis = 100, delayMillis = 50),
                            ),
                            exit = shrinkOut(
                                shrinkTowards = Alignment.CenterStart,
                                animationSpec = tween(durationMillis = 100),
                            ) + fadeOut(
                                animationSpec = tween(durationMillis = 100),
                            ),
                        ) {
                            Text(text = i18n.conversations.listTitle)
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            val offset by transition.animateDp { if (expanded) 0.dp else 8.dp }

            FloatingActionButton(
                modifier = Modifier.offset(x = offset),
                onClick = {
                    onCreateConversation()
                    if (expanded) stageControl.toggleNavigation()
                },
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_add),
                    contentDescription = i18n.conversations.newConversationAria,
                )
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> FullSizeProgressIndicator(modifier = Modifier.padding(paddingValues))

            else -> {
                val listState = rememberLazyListState()

                LaunchedEffect(selectedConversationId) {
                    if (selectedConversationId != null) {
                        val index = conversations.indexOfFirst { it.id == selectedConversationId }
                        listState.reveal(index)
                    }
                }

                transition.AnimatedVisibility(
                    modifier = Modifier.padding(paddingValues).fillMaxSize(),
                    visible = { it },
                    enter = fadeIn() + expandIn(expandFrom = Alignment.CenterStart),
                    exit = shrinkOut(shrinkTowards = Alignment.CenterStart) + fadeOut(),
                ) {
                    Column(
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            state = listState,
                            contentPadding = PaddingValues(
                                start = 8.dp,
                                end = 8.dp,
                                top = 8.dp,
                                bottom = 64.dp,
                            ),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            items(conversations, key = { it.id }) { conversation ->
                                ConversationListItem(
                                    conversation = conversation,
                                    selected = conversation.id == selectedConversationId,
                                    onClick = {
                                        onSelectConversation(conversation.id)
                                        if (expanded) stageControl.toggleNavigation()
                                    },
                                    onDelete = {
                                        onDeleteConversation(conversation.id)
                                    },
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 16.dp, top = 16.dp, end = 64.dp)
                                .fillMaxWidth()
                                .requiredHeight(56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            IconButton(onClick = {
                                onSettingsClick()
                            }) {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_settings),
                                    contentDescription = i18n.settings.listTitle,
                                )
                            }

                            DropdownMenu(
                                expanded = false,
                                onDismissRequest = {},
                            ) {
                                Text("Profile")
                            }
                        }
                    }
                }
            }
        }
    }
}

//@Preview
//@Composable
//private fun ConversationListPanelPreview() {
//    val repo = remember { InMemoryConversationRepository() }
//
//    // Seed preview data
//    LaunchedEffect(Unit) {
//        repo.create(
//            ConversationDigest(
//                id = "1",
//                title = "First",
//                createdAt = Instant.fromEpochMilliseconds(0),
//                updatedAt = Instant.fromEpochMilliseconds(0),
//                participants = emptyList(),
//                lastMessagePreview = "Hello world",
//                messageCount = 1,
//            )
//        )
//        repo.create(
//            ConversationDigest(
//                id = "2",
//                title = "Second",
//                createdAt = Instant.fromEpochMilliseconds(0),
//                updatedAt = Instant.fromEpochMilliseconds(0),
//                participants = emptyList(),
//                lastMessagePreview = "Another message",
//                messageCount = 3,
//            )
//        )
//    }
//
//    val vm = remember { ConversationListViewModel(repo) }
//
//    ConversationListScreen(
//        viewModel = vm,
//        modifier = Modifier.fillMaxSize(),
//        onSettingsClick = {},
//        selectedConversationId = null,
//        onCreateConversation = {},
//        onSelectConversation = {},
//        onDeleteConversation = {},
//    )
//}
