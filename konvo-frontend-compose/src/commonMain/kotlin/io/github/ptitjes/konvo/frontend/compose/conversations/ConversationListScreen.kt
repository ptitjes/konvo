package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.tooling.preview.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.storage.inmemory.*
import io.github.ptitjes.konvo.frontend.compose.*
import io.github.ptitjes.konvo.frontend.compose.resources.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.utils.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.*
import kotlin.time.*

@Composable
fun ConversationListScreen(
    viewModel: ConversationListViewModel = viewModel(),
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    val coroutineScope = rememberCoroutineScope()

    ConversationListScreen(
        modifier = modifier,
        viewModel = viewModel,
        expanded = navigator.navigationPaneState.targetValue.isExpanded,
        onExpandedToggle = { coroutineScope.launch { navigator.navigationPaneState.toggle() } },
        onSettingsClick = { coroutineScope.launch { navigator.openSettings() }},
        selectedConversationId = navigator.selectedConversationId,
        onCreateConversation = { navigator.navigateToNewConversation() },
        onSelectConversation = { navigator.navigateToConversation(it) },
        onDeleteConversation = {
            if (navigator.selectedConversationId == it) {
                navigator.navigateToNewConversation()
            }
        }
    )
}

/**
 * Conversation list panel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationListScreen(
    expanded: Boolean,
    onExpandedToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    selectedConversationId: String?,
    onCreateConversation: () -> Unit,
    onSelectConversation: (id: String) -> Unit,
    onDeleteConversation: (id: String) -> Unit,
    viewModel: ConversationListViewModel = viewModel(),
    modifier: Modifier = Modifier,
) {
    val conversations by viewModel.conversations.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(error) {
        if (error != null) {
            snackbarHostState.showSnackbar(message = error!!)
        }
    }

    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = MaterialTheme.colorScheme.onSurface

    val clickableModifier = if (!expanded) {
        val interactionSource = remember { MutableInteractionSource() }
        Modifier.clickable(interactionSource = interactionSource, indication = null) { onExpandedToggle() }
    } else Modifier

    val transition = updateTransition(expanded)

    Scaffold(
        modifier = modifier.then(clickableModifier),
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = contentColor,
                ),
                navigationIcon = {
                    IconButton(onClick = { onExpandedToggle() }) {
                        Icon(
                            painterResource(
                                if (expanded) Res.drawable.ic_left_panel_close
                                else Res.drawable.ic_left_panel_open
                            ),
                            contentDescription = "Menu",
                        )
                    }
                },
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
                            Text(text = strings.conversations.listTitle)
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
                    if (expanded) onExpandedToggle()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = strings.conversations.newConversationAria,
                )
            }
        }
    ) { paddingValues ->
        when {
            isLoading -> FullSizeProgressIndicator(modifier = Modifier.padding(paddingValues))
            conversations.isEmpty() -> EmptyConversationListPanel(
                modifier = Modifier.padding(paddingValues),
                onNewClick = onCreateConversation,
            )

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
                                        if (expanded) onExpandedToggle()
                                    },
                                    onDelete = {
                                        viewModel.delete(conversation)
                                        onDeleteConversation(conversation.id)
                                    },
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .padding(start = 16.dp, bottom = 16.dp, top = 16.dp, end = 72.dp)
                                .fillMaxWidth()
                                .requiredHeight(56.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            IconButton(onClick = {
                                onSettingsClick()
                            }) {
                                Icon(Icons.Filled.Settings, contentDescription = "Settings")
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

@Preview
@Composable
private fun ConversationListPanelPreview() {
    val repo = remember { InMemoryConversationRepository() }

    // Seed preview data
    LaunchedEffect(Unit) {
        repo.create(
            ConversationDigest(
                id = "1",
                title = "First",
                createdAt = Instant.fromEpochMilliseconds(0),
                updatedAt = Instant.fromEpochMilliseconds(0),
                participants = emptyList(),
                lastMessagePreview = "Hello world",
                messageCount = 1,
            )
        )
        repo.create(
            ConversationDigest(
                id = "2",
                title = "Second",
                createdAt = Instant.fromEpochMilliseconds(0),
                updatedAt = Instant.fromEpochMilliseconds(0),
                participants = emptyList(),
                lastMessagePreview = "Another message",
                messageCount = 3,
            )
        )
    }

    val vm = remember { ConversationListViewModel(repo) }

    ConversationListScreen(
        viewModel = vm,
        modifier = Modifier.fillMaxSize(),
        expanded = true,
        onSettingsClick = {},
        onExpandedToggle = {},
        selectedConversationId = null,
        onCreateConversation = {},
        onSelectConversation = {},
        onDeleteConversation = {},
    )
}
