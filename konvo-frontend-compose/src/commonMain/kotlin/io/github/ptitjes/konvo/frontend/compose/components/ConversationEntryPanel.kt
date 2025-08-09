package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.mikepenz.markdown.m3.*
import io.github.ptitjes.konvo.core.conversation.*
import kotlinx.serialization.json.*

@Composable
fun ConversationEventPanel(event: ConversationEvent) = when (event) {
    is ConversationEvent.UserMessage -> ConversationUserMessagePanel(event)
    is ConversationEvent.AssistantMessage -> ConversationAgentMessagePanel(event)
    is ConversationEvent.AssistantProcessing -> {}
    is ConversationEvent.AssistantToolUseResult -> ConversationAssistantToolUseResultPanel(event)
    is ConversationEvent.AssistantToolUseVetting -> ConversationAssistantToolUseVettingPanel(event)
    is ConversationEvent.ToolUseApproval -> {}
}

@Composable
fun ConversationUserMessagePanel(
    event: ConversationEvent.UserMessage,
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
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column {
                    MarkdownContent(
                        content = event.content,
                        textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    )

                    event.attachments.forEach { attachment ->
                        AttachmentView(attachment)
                    }
                }
            }
        }
    }
}

@Composable
fun ConversationAgentMessagePanel(
    event: ConversationEvent.AssistantMessage,
) {
    val horizontalArrangement = Arrangement.Start
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
    ) {
        MarkdownContent(
            content = event.content,
            textColor = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
fun ConversationAssistantToolUseVettingPanel(
    event: ConversationEvent.AssistantToolUseVetting,
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
                    text = "Tool use vetting",
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
    event: ConversationEvent.AssistantToolUseResult,
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
                                append("Agent called tool: ")
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                                    append(event.call.tool)
                                }
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.weight(1f),
                        )

                        Text("Details")

                        Icon(
                            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = if (expanded) "Collapse" else "Expand",
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
        contentDescription = "Success",
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier,
    )

    is ToolCallResult.ExecutionFailure -> Icon(
        imageVector = Icons.Filled.Error,
        contentDescription = "Failure",
        tint = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
}
