package io.github.ptitjes.konvo.frontend.compose.conversations.view.states

import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.dsl.ConversationViewStateContribution.*

sealed interface ToolUsageViewState : ConversationViewState.Item {

    data class Vetting(
        override val id: Any,
        val approvals: Map<ToolUsage.Call, ApprovalStatus>,
    ) : ToolUsageViewState {
        sealed interface ApprovalStatus {
            data object Pending : ApprovalStatus
            data object Approved : ApprovalStatus
            data class Denied(val reason: String) : ApprovalStatus
        }
    }

    data class Notification(
        override val id: Any,
        val call: ToolUsage.Call,
        val result: ToolUsage.CallResult,
    ) : ToolUsageViewState

    companion object : Contribution {
        override fun ContributionsScope.contribute() {
            onEvent<ToolUsage.Vetting> { event ->
                append(
                    ConversationViewState.Items,
                    Vetting(
                        id = event.id,
                        approvals = event.payload.calls.associateWith { Vetting.ApprovalStatus.Pending },
                    ),
                ) {
                    onEvent<ToolUsage.Approval> { state, approvalEvent ->
                        val incomingApprovals = approvalEvent.payload.approvals

                        val updatedApprovals = incomingApprovals.keys.fold(state.approvals) { existingApprovals, key ->
                            val newStatus by lazy {
                                val approved = incomingApprovals[key]
                                when (approved) {
                                    true -> Vetting.ApprovalStatus.Approved
                                    false -> Vetting.ApprovalStatus.Denied("Not specified")
                                    null -> Vetting.ApprovalStatus.Pending
                                }
                            }
                            if (key in existingApprovals) existingApprovals + (key to newStatus) else existingApprovals
                        }

                        val done = updatedApprovals.all { (_, status) ->
                            status !is Vetting.ApprovalStatus.Pending
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
