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
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import org.jetbrains.compose.resources.*

@Composable
fun ConversationScreen(
    conversationId: String,
    viewModel: ConversationViewModel = viewModel(key = conversationId),
    navigator: Navigator,
    modifier: Modifier = Modifier,
) {
    CompositionLocalProvider(LocalViewRegistry provides viewModel.componentRegistry) {
        ConversationScreen(
            conversationId = conversationId,
            viewModel = viewModel,
            modifier = modifier,
        )
    }
}

/**
 * A screen that displays a conversation with a top app bar.
 *
 * @param viewModel The view model of the conversation to display
 * @param modifier The modifier to apply to this component
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    conversationId: String,
    viewModel: ConversationViewModel = viewModel(key = conversationId),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()

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
                TopAppBar(
                    modifier = Modifier.background(topBarGradient),
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    title = {
                        when (val state = state) {
                            is ConversationViewState.Loading -> Text(
                                text = "Conversation title",
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp)
                                    .fillMaxWidth().placeholder(
                                        visible = true,
                                        highlight = PlaceholderHighlight.shimmer(),
                                    ),
                            )

                            is ConversationViewState.Loaded -> with(viewModel.conversation) {
                                EditableConversationTitle(
                                    conversationTitle = state.preview.title,
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        LocalCenterStageControl.current.NavigationButton {
                            Icon(
                                modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                                painter = painterResource(Res.drawable.ic_chat),
                                contentDescription = strings.conversations.conversationAria
                            )
                        }
                    },
                    actions = {
                        LocalCenterStageControl.current.ExtraPaneButton()
                    }
                )
            },
            bottomBar = {
                // TODO on mobile only show bottom only when at the bottom of the conversation log or dragging up
                when (val state = state) {
                    is ConversationViewState.Loading -> {}
                    is ConversationViewState.Loaded -> AnimatedVisibility(
                        visible = state.items.isNotEmpty(),
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
                            with(viewModel.conversation) {
                                ConversationSuggestions()
                                ConversationInputBox()
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            when (val state = state) {
                is ConversationViewState.Loading -> FullSizeProgressIndicator(
                    modifier = Modifier.padding(paddingValues),
                )

                is ConversationViewState.Loaded -> with(viewModel.conversation) {
                    ConversationPane(
                        state = state,
                        modifier = Modifier.fillMaxSize(),
                        paddingValues = paddingValues,
                        sharedTransitionScope = this@SharedTransitionLayout,
                    )
                }
            }
        }
    }
}
