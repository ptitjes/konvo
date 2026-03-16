package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.views

import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi.*

fun ConversationViewStateMaintainer.setupCoreViewStateProducers() {
    contributeViewStates(PreviewViewState.Companion)
    contributeViewStates(PresenceViewState.Companion)
    contributeViewStates(AgentViewState.Companion)
    contributeViewStates(MessagingViewState.Companion)
    contributeViewStates(ToolUsageViewState.Companion)
}
