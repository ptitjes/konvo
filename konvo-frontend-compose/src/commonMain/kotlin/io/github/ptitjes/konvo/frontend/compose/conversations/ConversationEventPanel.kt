package io.github.ptitjes.konvo.frontend.compose.conversations

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
import com.mikepenz.markdown.m3.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.*
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.*
import io.github.ptitjes.konvo.frontend.compose.conversations.views.*
import io.github.ptitjes.konvo.frontend.compose.resources.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.jetbrains.compose.resources.*

@Composable
fun ConversationEventPanel(itemViewState: ConversationViewState.Item, conversation: ConversationUserView) =
    when (itemViewState) {
        is MessagingViewState.UserMessage -> UserMessagePanel(itemViewState)
        is MessagingViewState.AssistantMessage -> AgentMessagePanel(itemViewState)
        is ToolUsageViewState.Vetting -> ToolUsageVettingPanel(itemViewState, conversation)
        is ToolUsageViewState.Notification -> ToolUsageNotificationPanel(itemViewState)
        else -> {}
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
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                ) {
                    SelectionContainer {
                        MarkdownContent(
                            state = itemViewState.markdownState,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
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
private fun ToolUsageVettingPanel(
    viewState: ToolUsageViewState.Vetting,
    conversation: ConversationUserView,
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
                        AskIcon()

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
                                    FailureIcon()
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
                                    SuccessIcon()
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
                                    FailureIcon()
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
                    ToolArgumentsTable(
                        arguments = call.arguments,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
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
                ResultIcon(viewState.result)

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

                Text(strings.conversations.detailsLabel)
            },
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                ToolArgumentsTable(
                    arguments = viewState.call.arguments,
                )

                when (val result = viewState.result) {
                    is CallResult.Success -> {
                        Markdown(
                            content = "```json\n${Json.encodeToString(result.value)}\n```",
                            colors = markdownColor(text = LocalContentColor.current),
                        )
                    }

                    else -> {
                        val failure = result as CallResult.ExecutionFailure
                        Markdown(
                            content = "```\n${failure.reason}\n```",
                            colors = markdownColor(text = LocalContentColor.current),
                        )
                    }
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
        TextButton(
            onClick = { expanded = !expanded },
            shape = RoundedCornerShape(4.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            enabled = collapsable,
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
    arguments: Map<String, JsonElement>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
    ) {
        val headerColumnWidth = remember { mutableStateOf<Int?>(null) }

        arguments.entries.forEach { (name, value) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "$name:",
                    fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                    modifier = Modifier.padding(start = 4.dp).withSharedWidth(headerColumnWidth)
                )

                val code = remember(value) {
                    prettyJson.encodeToString(JsonElement.serializer(), value)
                }

                Markdown(
                    content = "```json\n$code\n```",
                    colors = markdownColor(text = LocalContentColor.current),
                )
            }
        }
    }
}

private val prettyJson = Json { prettyPrint = true }

private fun Modifier.withSharedWidth(headerColumnWidth: MutableState<Int?>) = layout { measurable, constraints ->
    val placeable = measurable.measure(constraints)

    val existingWidth = headerColumnWidth.value ?: 0
    val maxWidth = maxOf(existingWidth, placeable.width)

    if (maxWidth > existingWidth) {
        headerColumnWidth.value = maxWidth
    }

    layout(width = maxWidth, height = placeable.height) {
        placeable.placeRelative(0, 0)
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
