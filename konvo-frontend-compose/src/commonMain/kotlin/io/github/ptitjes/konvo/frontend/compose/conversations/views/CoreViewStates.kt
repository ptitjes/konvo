package io.github.ptitjes.konvo.frontend.compose.conversations.views

import io.github.ptitjes.konvo.frontend.compose.conversations.spi.ConversationViewStateMaintainer

fun ConversationViewStateMaintainer.setupCoreViewStateProducers() {
    contributeViewStates(AgentViewState.Companion)
    contributeViewStates(MessagingViewState.Companion)
    contributeViewStates(ToolUsageViewState.Companion)
}
