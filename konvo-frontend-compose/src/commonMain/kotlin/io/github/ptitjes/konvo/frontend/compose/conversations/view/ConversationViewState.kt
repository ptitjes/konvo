package io.github.ptitjes.konvo.frontend.compose.conversations.view

import com.mikepenz.markdown.model.State
import io.github.ptitjes.konvo.core.conversations.model.ConversationDigest
import io.github.ptitjes.konvo.core.conversations.model.events.Messaging
import io.github.ptitjes.konvo.core.conversations.model.events.ToolUsage

sealed interface ConversationViewState {
    data object Loading : ConversationViewState
    data class Loaded(
        val conversation: ConversationDigest,
        val items: List<Item>,
        val isProcessing: Boolean,
    ) : ConversationViewState

    interface Item {
        val id: Any
    }
}

sealed interface ItemViewState : ConversationViewState.Item {

    data class UserMessage(
        override val id: Any,
        val details: Messaging.Message,
        val markdownState: State,
    ) : ItemViewState

    data class AssistantMessage(
        override val id: Any,
        val details: Messaging.Message,
        val markdownState: State,
    ) : ItemViewState

    data class ToolUseVetting(
        override val id: Any,
        val approvals: Map<ToolUsage.Call, ApprovalStatus>,
    ) : ItemViewState {
        sealed interface ApprovalStatus {
            data object Pending : ApprovalStatus
            data object Approved : ApprovalStatus
            data class Denied(val reason: String) : ApprovalStatus
        }
    }

    data class ToolUseNotification(
        override val id: Any,
        val call: ToolUsage.Call,
        val result: ToolUsage.CallResult,
    ) : ItemViewState
}
