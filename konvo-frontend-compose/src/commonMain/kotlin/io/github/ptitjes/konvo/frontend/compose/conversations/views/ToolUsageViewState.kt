package io.github.ptitjes.konvo.frontend.compose.conversations.views

import com.mikepenz.markdown.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.ConversationViewStates.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.utils.*
import kotlinx.serialization.json.*

sealed interface ToolUsageViewState : ConversationViewState.Item {

    data class Vetting(
        override val id: Any,
        val callsArgumentsMarkdownStates: Map<ToolUsage.Call, Map<String, State>>,
        val approvals: Map<ToolUsage.Call, Status>,
    ) : ToolUsageViewState {
        sealed interface Status {
            data object Pending : Status
            data object Approved : Status
            data class Denied(val reason: String) : Status
        }
    }

    data class Notification(
        override val id: Any,
        val call: ToolUsage.Call,
        val result: ToolUsage.CallResult,
        val argumentsMarkdownStates: Map<String, State>,
        val resultMarkdownState: State,
    ) : ToolUsageViewState

    companion object : Contribution {
        override fun CreateScope.contribute() {
            onEvent<ToolUsage.Vetting> { event ->
                append(
                    ConversationViewState.Items,
                    Vetting(
                        id = event.id,
                        callsArgumentsMarkdownStates = event.payload.calls.associateWith { parseMarkdownArguments(it) },
                        approvals = event.payload.calls.associateWith { Vetting.Status.Pending },
                    ),
                ) {
                    onEvent<ToolUsage.Approval> { state, approvalEvent ->
                        val incomingApprovals = approvalEvent.payload.approvals

                        val updatedApprovals =
                            incomingApprovals.fold(state.approvals) { existingApprovals, (key, approved) ->
                            val newStatus by lazy {
                                when (approved) {
                                    true -> Vetting.Status.Approved
                                    false -> Vetting.Status.Denied("Not specified")
                                }
                            }
                            if (key in existingApprovals) existingApprovals + (key to newStatus) else existingApprovals
                        }

                        val done = updatedApprovals.all { (_, status) ->
                            status !is Vetting.Status.Pending
                        }
                        if (done) freeze()

                        state.copy(
                            approvals = updatedApprovals
                        )
                    }
                }
            }

            onEvent<ToolUsage.Notification> { event ->
                val call = event.payload.call
                val result = event.payload.result
                append(
                    ConversationViewState.Items,
                    Notification(
                        id = event.id,
                        call = call,
                        result = result,
                        argumentsMarkdownStates = parseMarkdownArguments(call),
                        resultMarkdownState = parseMarkdownResult(result),
                    ),
                )
            }
        }
    }
}

private suspend fun parseMarkdownArguments(call: ToolUsage.Call): Map<String, State> {
    return call.arguments.mapValues { (_, value) ->
        val code = jsonFormat.encodeToString(value)
        parseMarkdown("```json\n$code\n```")
    }
}

private suspend fun parseMarkdownResult(result: ToolUsage.CallResult): State {
    var result1 = result
    return parseMarkdown(
        when (val result = result1) {
            is ToolUsage.CallResult.Success -> "```json\n${jsonFormat.encodeToString(result.value)}\n```"
            is ToolUsage.CallResult.ExecutionFailure -> "```\n${result.reason}\n```"
        }
    )
}

private val jsonFormat = Json { prettyPrint = true }
