package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import com.slack.circuit.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*

class ConversationNavigator(private val navigator: Navigator) {
    private val backStack: List<ConversationsScreen>
        get() = navigator.peekBackStack().filterIsInstance<ConversationsScreen>().reversed()

    val selectedConversationId: String?
        get() {
            val lastConversationEntry = backStack.lastOrNull()
            return (lastConversationEntry as? ConversationScreen)?.id
        }

    fun goToConversation(conversationId: String) {
        if (selectedConversationId == conversationId) return
        navigator.goTo(ConversationScreen(conversationId))
    }

    fun goToNewConversation() {
        navigator.goTo(NewConversationScreen)
    }

    fun openSettings() {
        onNavigateToSettings(null)
    }

    fun openSettingsSection(titleKey: String) {
        onNavigateToSettings(titleKey)
    }

    private fun onNavigateToSettings(key: String?) {
        navigator.goTo(SettingsScreen(key))
    }
}
