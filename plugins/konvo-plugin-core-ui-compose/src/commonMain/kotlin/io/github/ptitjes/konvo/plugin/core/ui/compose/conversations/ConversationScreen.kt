package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.unit.*
import com.eygraber.compose.placeholder.*
import com.eygraber.compose.placeholder.material3.*
import com.slack.circuit.retained.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.conversations.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.views.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import io.github.ptitjes.syrup.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import org.jetbrains.compose.resources.*
import kotlin.time.*

data class ConversationScreen(val id: String) : ConversationsScreen {
    override fun toString(): String = "conversation/$id"

    sealed interface State : CircuitUiState {
        data object Loading : State
        data class Loaded(
            val id: String,
            val conversation: ConversationViewState,
            val device: InteractionDevice.User,
        ) : State
    }
}

class ConversationPresenter(
    private val screen: ConversationScreen,
    private val navigator: ConversationNavigator,
    private val conversationRepository: ConversationRepository,
    private val conversationManager: ConversationManager,
    private val pluginContext: PluginContext,
) : Presenter<ConversationScreen.State> {
    @Composable
    override fun present(): ConversationScreen.State {
        val conversationId = screen.id
        val conversation = conversationManager.getConversation(conversationId)
        val state by conversation.state.collectAsState()

        var transcriptLoaded by rememberRetained { mutableStateOf(false) }

        val stateUpdater = rememberRetained {
            ConversationViewStateMaintainer().apply {
                setupCoreViewStateProducers()
            }
        }

        LaunchedEffect(stateUpdater, transcriptLoaded) {
            if (transcriptLoaded) return@LaunchedEffect

            val state = conversation.awaitConversationLoaded()
            val transcriptHandled = Job()

            launch {
                val device = conversation.newUserDevice()
                device.actions.buffer(Channel.UNLIMITED).collect { event ->
                    transcriptHandled.join()

                    stateUpdater.handleEvent(event)

                    conversationRepository.updateDigest(
                        state.digest.updateFrom(stateUpdater.state, "user")
                    )
                }
            }

            // Extract actions from transcript for view state processing
            val actions = state.transcript.actions
            stateUpdater.handleTranscript(actions)

            conversationRepository.updateDigest(
                state.digest.updateFrom(stateUpdater.state, "user")
            )

            transcriptHandled.complete()
            transcriptLoaded = true
        }

        return when (state) {
            is ConversationState.Loaded if (transcriptLoaded) -> ConversationScreen.State.Loaded(
                id = conversationId,
                conversation = stateUpdater.state,
                device = conversation.newUserDevice(),
            )

            else -> ConversationScreen.State.Loading
        }
    }
}

private fun ConversationDigest.updateFrom(
    state: ConversationViewState,
    participantId: String,
): ConversationDigest {
    val preview = state.preview
    val presence = state.presence
    val items = state.items

    val participant = presence.keys.firstOrNull { it.id == participantId } ?: return this
    val lastViewTimestamp = presence[participant]?.lastViewTimestamp

    return copy(
        title = preview.title,
        updatedAt = Clock.System.now(),
        participants = presence.keys.toList(),
        lastMessagePreview = preview.lastMessagePreview,
        messageCount = items.size,
        unreadMessageCount =
            if (lastViewTimestamp != null) items.count { it.timestamp > lastViewTimestamp } else items.size,
    )
}

@Composable
fun ConversationScreen(
    state: ConversationScreen.State,
    modifier: Modifier = Modifier,
) {
    // TODO use a service and inject it here
    val viewRegistry = remember {
        ConversationViewRegistry.Builder().apply {
            contributeComponents(CoreComponents)
            // TODO
            // componentContributions.forEach { contributeComponents(it) }
        }.build()
    }

    CompositionLocalProvider(LocalViewRegistry provides viewRegistry) {
        val barHeightPx = with(LocalDensity.current) { 64.dp.toPx() }

        val backgroundColor = MaterialTheme.colorScheme.background
        val topBarGradient = Brush.linearGradient(
            0.0f to backgroundColor,
            1.0f to Color.Transparent,
            start = Offset(0.0f, 0.0f),
            end = Offset(0.0f, barHeightPx)
        )
        val bottomBarGradient = Brush.linearGradient(
            0.0f to Color.Transparent,
            0.4f to backgroundColor,
            0.6f to backgroundColor,
            1.0f to backgroundColor.copy(alpha = 0.65f),
            start = Offset(0.0f, 0.0f),
            end = Offset(0.0f, barHeightPx)
        )

        SharedTransitionLayout {
            Scaffold(
                modifier = modifier,
                topBar = {
                    @OptIn(ExperimentalMaterial3Api::class)
                    TopAppBar(
                        modifier = Modifier.background(topBarGradient),
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        title = {
                            when (val state = state) {
                                is ConversationScreen.State.Loading -> Text(
                                    text = "Conversation title",
                                    modifier = Modifier
                                        .padding(start = 16.dp, end = 16.dp)
                                        .fillMaxWidth().placeholder(
                                            visible = true,
                                            highlight = PlaceholderHighlight.shimmer(),
                                        ),
                                )

                                is ConversationScreen.State.Loaded -> with(state.device) {
                                    EditableConversationTitle(
                                        conversationTitle = state.conversation.preview.title,
                                    )
                                }
                            }
                        },
                        navigationIcon = {
                            LocalCenterStageControl.current.ContentNavigationButton {
                                Icon(
                                    modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                                    painter = painterResource(Res.drawable.ic_chat),
                                    contentDescription = i18n.conversations.conversationAria
                                )
                            }
                        },
                        actions = {
                            LocalCenterStageControl.current.ContentExtraButton()
                        }
                    )
                },
                bottomBar = {
                    // TODO on mobile only show bottom only when at the bottom of the conversation log or dragging up
                    when (val state = state) {
                        is ConversationScreen.State.Loading -> {}
                        is ConversationScreen.State.Loaded -> AnimatedVisibility(
                            visible = state.conversation.items.isNotEmpty(),
                            enter = expandVertically(),
                            exit = shrinkVertically(),
                        ) {
                            Column(
                                modifier = Modifier
                                    .background(bottomBarGradient)
                                    .padding(vertical = 16.dp)
                                    .fillMaxWidth()
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState("input-box"),
                                        animatedVisibilityScope = this,
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                with(state.device) {
                                    ConversationSuggestions()
                                    ConversationInputBox()
                                }
                            }
                        }
                    }
                }
            ) { paddingValues ->
                when (val state = state) {
                    is ConversationScreen.State.Loading -> FullSizeProgressIndicator(
                        modifier = Modifier.padding(paddingValues),
                    )

                    is ConversationScreen.State.Loaded -> with(state.device) {
                        ConversationPane(
                            state = state.conversation,
                            modifier = Modifier.fillMaxSize(),
                            paddingValues = paddingValues,
                            sharedTransitionScope = this@SharedTransitionLayout,
                        )
                    }
                }
            }
        }
    }
}
