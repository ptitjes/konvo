package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.selection.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.mikepenz.markdown.m3.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging.*
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.states.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlinx.coroutines.*

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
                Column {
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
        Column {
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
) {
    BorderedPanel {
        Column {
            for ((call, status) in viewState.approvals) {
                ExpandableBox(
                    collapsable = status !is ToolUsageViewState.Vetting.ApprovalStatus.Pending,
                    header = {
                        AskIcon()

                        Text(
                            text = buildAnnotatedString {
                                append(strings.conversations.agentWantsToCallToolPrefix)
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(call.tool)
                                }
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                        )

                        when (status) {
                            is ToolUsageViewState.Vetting.ApprovalStatus.Pending -> {
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

                            is ToolUsageViewState.Vetting.ApprovalStatus.Approved -> {
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

                            is ToolUsageViewState.Vetting.ApprovalStatus.Denied -> {
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
) {
    BorderedPanel {
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
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f),
                )

                Text(strings.conversations.detailsLabel)
            },
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ToolArgumentsTable(
                    arguments = viewState.call.arguments,
                )

                when (val result = viewState.result) {
                    is CallResult.Success -> {
                        Markdown(
                            content = "```json\n${result.text}\n```",
                            typography = markdownTypography(code = MaterialTheme.typography.bodyMedium),
                            colors = markdownColor(text = MaterialTheme.colorScheme.onBackground),
                        )
                    }

                    else -> {
                        val failure = result as CallResult.ExecutionFailure
                        Markdown(
                            content = "```\n${failure.reason}\n```",
                            typography = markdownTypography(code = MaterialTheme.typography.bodyMedium),
                            colors = markdownColor(text = MaterialTheme.colorScheme.onBackground),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BorderedPanel(content: @Composable () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Surface(
            modifier = Modifier.border(
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(8.dp),
            ),
            color = MaterialTheme.colorScheme.background,
        ) {
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
                    Icon(
                        imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription =
                            if (expanded) strings.conversations.collapseAria
                            else strings.conversations.expandAria,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }

        if (!collapsable || expanded) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
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
        imageVector = Icons.Filled.QuestionMark,
        contentDescription = "Question", // TODO
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
private fun SuccessIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = strings.conversations.successAria,
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )
}

@Composable
private fun FailureIcon(modifier: Modifier = Modifier) {
    Icon(
        imageVector = Icons.Filled.Error,
        contentDescription = strings.conversations.failureAria,
        tint = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
}
