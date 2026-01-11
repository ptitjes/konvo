package io.github.ptitjes.konvo.frontend.compose.conversations.view.states

import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.dsl.ConversationViewStates.*

sealed interface ToolUsageViewState : ConversationViewState.Item {

    data class Vetting(
        override val id: Any,
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
    ) : ToolUsageViewState

    companion object : Contribution {
        override fun CreateScope.contribute() {
            onEvent<ToolUsage.Vetting> { event ->
                append(
                    ConversationViewState.Items,
                    Vetting(
                        id = event.id,
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
                append(
                    ConversationViewState.Items,
                    Notification(
                        id = event.id,
                        call = event.payload.call,
                        result = event.payload.result,
                    ),
                )
            }
        }
    }
}
