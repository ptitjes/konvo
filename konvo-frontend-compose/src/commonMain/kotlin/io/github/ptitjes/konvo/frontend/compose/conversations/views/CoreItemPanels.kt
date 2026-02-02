package io.github.ptitjes.konvo.frontend.compose.conversations.views

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.selection.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.mikepenz.markdown.model.State
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.*
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.*
import io.github.ptitjes.konvo.frontend.compose.conversations.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.*
import io.github.ptitjes.konvo.frontend.compose.resources.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.*
import kotlin.math.*

internal fun ConversationViews.ContributionScope.coreItemPanels() {
    ConversationPane.ItemPanels {
        put<MessagingViewState.UserMessage> { UserMessagePanel(it) }
        put<MessagingViewState.AssistantMessage> { AgentMessagePanel(it) }
        put<ToolUsageViewState.Vetting> { ToolUsageVettingPanel(it) }
        put<ToolUsageViewState.Notification> { ToolUsageNotificationPanel(it) }
    }
}

@Composable
private fun UserMessagePanel(
    itemViewState: MessagingViewState.UserMessage,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(.80f),
            horizontalArrangement = Arrangement.End,
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    SelectionContainer {
                        MarkdownContent(
                            state = itemViewState.markdownState,
                            textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }

                    itemViewState.details.content.filterIsInstance<Part.Media>().forEach { media ->
                        AttachmentView(media.media)
                    }
                }
            }
        }
    }
}

@Composable
private fun AgentMessagePanel(
    itemViewState: MessagingViewState.AssistantMessage,
) {
    val horizontalArrangement = Arrangement.Start
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
        ) {
            SelectionContainer {
                MarkdownContent(
                    state = itemViewState.markdownState,
                    textColor = MaterialTheme.colorScheme.onBackground,
                )
            }

            itemViewState.details.content.filterIsInstance<Part.Media>().forEach { media ->
                AttachmentView(media.media)
            }
        }
    }
}

@Composable
context(conversation: ConversationUserView)
private fun ToolUsageVettingPanel(
    viewState: ToolUsageViewState.Vetting,
    modifier: Modifier = Modifier,
) {
    BorderedPanel(
        modifier = modifier,
    ) {
        Column {
            for ((call, status) in viewState.approvals) {
                ExpandableBox(
                    collapsable = status !is ToolUsageViewState.Vetting.Status.Pending,
                    header = {
                        AskIcon(modifier = Modifier.requiredSize(20.dp))

                        Text(
                            text = buildAnnotatedString {
                                append(strings.conversations.agentWantsToCallToolPrefix)
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(call.tool)
                                }
                            },
                            fontSize = MaterialTheme.typography.titleSmall.fontSize,
                            modifier = Modifier.weight(1f),
                        )

                        when (status) {
                            is ToolUsageViewState.Vetting.Status.Pending -> {
                                val coroutineScope = rememberCoroutineScope()

                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            conversation.sendToolUseApproval(mapOf(call to false))
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                ) {
                                    FailureIcon(modifier = Modifier.requiredSize(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Deny")
                                }

                                TextButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            conversation.sendToolUseApproval(mapOf(call to true))
                                        }
                                    },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                                ) {
                                    SuccessIcon()
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Approve")
                                }
                            }

                            is ToolUsageViewState.Vetting.Status.Approved -> {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    SuccessIcon(modifier = Modifier.requiredSize(20.dp))
                                    Text(
                                        text = "Approved",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            }

                            is ToolUsageViewState.Vetting.Status.Denied -> {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    FailureIcon(modifier = Modifier.requiredSize(20.dp))
                                    Text(
                                        text = "Denied",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        }
                    },
                ) {
                    val argumentsState = viewState.callsArgumentsMarkdownStates[call]
                    if (argumentsState != null) {
                        ToolArgumentsTable(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            argumentsState = argumentsState,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolUsageNotificationPanel(
    viewState: ToolUsageViewState.Notification,
    modifier: Modifier = Modifier,
) {
    BorderedPanel(
        modifier = modifier,
    ) {
        ExpandableBox(
            header = {
                ResultIcon(modifier = Modifier.requiredSize(20.dp), result = viewState.result)

                Text(
                    text = buildAnnotatedString {
                        append(strings.conversations.agentCalledToolPrefix)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(viewState.call.tool)
                        }
                    },
                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                    modifier = Modifier.weight(1f),
                )

                Text(
                    text = strings.conversations.detailsLabel,
                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                )
            },
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                ToolArgumentsTable(
                    modifier = Modifier.fillMaxWidth(),
                    argumentsState = viewState.argumentsMarkdownStates,
                )

                SelectionContainer {
                    MarkdownContent(
                        modifier = modifier.clipToBounds().fillMaxWidth(),
                        state = viewState.resultMarkdownState,
                        textColor = LocalContentColor.current,
                    )
                }
            }
        }
    }
}

@Composable
private fun BorderedPanel(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier
            .border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(8.dp),
            ),
        color = MaterialTheme.colorScheme.background,
    ) {
        val lighter = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        CompositionLocalProvider(LocalContentColor provides lighter) {
            content()
        }
    }
}

@Composable
private fun ExpandableBox(
    collapsable: Boolean = true,
    modifier: Modifier = Modifier,
    header: @Composable RowScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp).heightIn(min = 36.dp)
                .clickable(enabled = collapsable) { expanded = !expanded },
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                header()

                if (collapsable) {
                    val rotation by animateFloatAsState(if (expanded) 180f else 0f)
                    Icon(
                        modifier = Modifier.rotate(rotation),
                        painter = painterResource(Res.drawable.ic_expand_more),
                        contentDescription =
                            if (expanded) strings.conversations.collapseAria
                            else strings.conversations.expandAria,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        AnimatedVisibility(!collapsable || expanded) {
            content()
        }
    }
}

@Composable
private fun ToolArgumentsTable(
    argumentsState: Map<String, State>,
    modifier: Modifier = Modifier,
) {
    ToolArgumentsTableLayout(
        modifier = modifier,
        arguments = argumentsState,
        keyContent = { key ->
            Text(
                text = "$key:",
                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                modifier = Modifier.padding(start = 4.dp, end = 8.dp),
            )
        },
        valueContent = { value ->
            MarkdownContent(
                modifier = Modifier.fillMaxSize(),
                state = value,
                textColor = LocalContentColor.current,
            )
        },
    )
}

@Composable
private fun <K, V> ToolArgumentsTableLayout(
    arguments: Map<K, V>,
    modifier: Modifier = Modifier,
    keyContent: @Composable (K) -> Unit,
    valueContent: @Composable (V) -> Unit,
) {
    Layout(
        modifier = modifier, content = {
            arguments.entries.forEach { (key, value) ->
                Box { keyContent(key) }
                Box { valueContent(value) }
            }
        }) { measurables, constraints ->
        val rowCount = measurables.size / 2
        val keyMeasureables = measurables.filterIndexed { index, _ -> index % 2 == 0 }
        val valueMeasureables = measurables.filterIndexed { index, _ -> index % 2 == 1 }

        val keyConstraints = constraints.copy(minWidth = 0)
        val keyPlaceables = keyMeasureables.map { it.measure(keyConstraints) }

        val keyColumnWidth = keyPlaceables.maxOfOrNull { it.width } ?: 0

        val valueConstraints = constraints.copy(
            minWidth = constraints.minWidth - keyColumnWidth,
            maxWidth = constraints.maxWidth - keyColumnWidth,
        )

        val valuePlaceables = valueMeasureables.map { it.measure(valueConstraints) }

        val rowHeights = (0 until rowCount).map { rowIndex ->
            max(keyPlaceables[rowIndex].height, valuePlaceables[rowIndex].height)
        }

        val totalWidth = keyColumnWidth + valuePlaceables.maxOf { it.width }
        val totalHeight = rowHeights.sum()

        layout(width = totalWidth, height = totalHeight) {
            var currentY = 0
            (0 until rowCount).forEach { rowIndex ->
                val keyPlaceable = keyPlaceables[rowIndex]
                val valuePlaceable = valuePlaceables[rowIndex]

                val keyHeight = keyPlaceable.height
                val valueHeight = valuePlaceable.height
                val rowHeight = max(keyHeight, valueHeight)

                keyPlaceable.placeRelative(0, currentY + rowHeight / 2 - keyHeight / 2)
                valuePlaceable.placeRelative(keyColumnWidth, currentY + rowHeight / 2 - valueHeight / 2)

                currentY += rowHeight
            }
        }
    }
}

@Composable
private fun ResultIcon(
    result: CallResult,
    modifier: Modifier = Modifier,
) = when (result) {
    is CallResult.Success -> SuccessIcon(modifier)
    is CallResult.ExecutionFailure -> FailureIcon(modifier)
}

@Composable
private fun AskIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(Res.drawable.ic_question_mark),
        contentDescription = "Question", // TODO
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
private fun SuccessIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(Res.drawable.ic_check_circle),
        contentDescription = strings.conversations.successAria,
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
private fun FailureIcon(modifier: Modifier = Modifier) {
    Icon(
        painter = painterResource(Res.drawable.ic_error),
        contentDescription = strings.conversations.failureAria,
        tint = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
}
