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
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import kotlinx.serialization.json.*

@Composable
fun ConversationEventPanel(eventViewState: EventViewState) = when (eventViewState) {
    is EventViewState.UserMessage -> ConversationUserMessagePanel(eventViewState)
    is EventViewState.AssistantMessage -> ConversationAgentMessagePanel(eventViewState)
    is EventViewState.ToolUseNotification -> ConversationAssistantToolUseResultPanel(eventViewState.event)
    is EventViewState.ToolUseVetting -> ConversationAssistantToolUseVettingPanel(eventViewState.event)
}

@Composable
fun ConversationUserMessagePanel(
    eventViewState: EventViewState.UserMessage,
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
                            state = eventViewState.markdownState,
                            textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        )
                    }

                    eventViewState.event.attachments.forEach { attachment ->
                        AttachmentView(attachment)
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationAgentMessagePanel(
    eventViewState: EventViewState.AssistantMessage,
) {
    val horizontalArrangement = Arrangement.Start
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
    ) {
        SelectionContainer {
            MarkdownContent(
                state = eventViewState.markdownState,
                textColor = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

@Composable
fun ConversationAssistantToolUseVettingPanel(
    event: Event.ToolUseVetting,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = strings.conversations.toolUseVettingTitle,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(Modifier.height(8.dp))

                val json = Json { prettyPrint = true }
                event.calls.forEach { call ->
                    Text(
                        text = "• ${call.tool} (id=${call.id})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    val argsJson = json.encodeToString(
                        JsonObject.serializer(),
                        JsonObject(call.arguments)
                    )
                    Markdown(
                        content = "```json\n$argsJson\n```",
                        colors = markdownColor(text = MaterialTheme.colorScheme.onBackground),
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun ConversationAssistantToolUseResultPanel(
    event: Event.ToolUseNotification,
) {
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
            var expanded by remember { mutableStateOf(false) }
            val result = event.result

            Column(modifier = Modifier) {
                TextButton(
                    onClick = { expanded = !expanded },
                    shape = RoundedCornerShape(4.dp),
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ResultIcon(result)

                        Text(
                            text = buildAnnotatedString {
                                append(strings.conversations.agentCalledToolPrefix)
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(event.call.tool)
                                }
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                        )

                        Text(strings.conversations.detailsLabel)

                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription =
                                if (expanded) strings.conversations.collapseAria
                                else strings.conversations.expandAria,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                if (expanded) {
                    ToolArgumentsTable(
                        arguments = event.call.arguments,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )

                    when (result) {
                        is ToolCallResult.Success -> {
                            Markdown(
                                content = result.text,
                                colors = markdownColor(text = MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            )
                        }

                        else -> {
                            val failure = result as ToolCallResult.ExecutionFailure
                            Markdown(
                                content = "```\n${failure.reason}\n```",
                                colors = markdownColor(text = MaterialTheme.colorScheme.onBackground),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ResultIcon(
    result: ToolCallResult,
    modifier: Modifier = Modifier,
) = when (result) {
    is ToolCallResult.Success -> Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = strings.conversations.successAria,
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )

    is ToolCallResult.ExecutionFailure -> Icon(
        imageVector = Icons.Filled.Error,
        contentDescription = strings.conversations.failureAria,
        tint = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
}
