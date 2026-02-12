package io.github.ptitjes.konvo.frontend.compose.conversations

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
import io.github.ptitjes.konvo.frontend.compose.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.*
import io.github.ptitjes.konvo.frontend.compose.resources.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
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

    Scaffold(
        modifier = modifier,
        topBar = {
            val barHeightPx = with(LocalDensity.current) { 64.dp.toPx() }

            val transparentToBlack =  Brush.linearGradient(
                0.0f to MaterialTheme.colorScheme.background,
                1.0f to Color.Transparent,
                start = Offset(0.0f, 0.0f),
                end = Offset(0.0f, barHeightPx)
            )

            TopAppBar(
                modifier = Modifier.background(transparentToBlack),
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

                        is ConversationViewState.Loaded -> EditableConversationTitle(
                            conversation = state.digest,
                            onTitleChange = { viewModel.updateTitle(it) }
                        )
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
        }
    ) { paddingValues ->
        when (val state = state) {
            is ConversationViewState.Loading -> FullSizeProgressIndicator()
            is ConversationViewState.Loaded -> with(viewModel.conversation) {
                ConversationPane(
                    state = state,
                    modifier = Modifier.fillMaxSize(),
                    onUpdateLastReadMessageIndex = viewModel::updateLastReadMessageIndex,
                    paddingValues = paddingValues,
                )
            }
        }
    }
}
