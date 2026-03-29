package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.*
import androidx.lifecycle.*
import androidx.navigation3.runtime.*
import com.slack.circuit.foundation.*
import com.slack.circuit.foundation.navstack.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.screen.Screen
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*

/**
 * ViewModel managing the current high-level application navigation state.
 */
internal class MainScreenViewModel(
    private val settingsSectionManager: SettingsSectionManager,
) : ViewModel() {

    private val navStack = SaveableNavStack(Destination.Conversation.List).apply {
        push(Destination.Conversation.New)
    }

    private val navigator = Navigator(navStack) { }

    val conversationBackStack = navigator.backStack<Destination>()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = null,
        )

    private val navigationPaneState = PaneState.Companion()
    private val extraPaneState = PaneState.Companion()

    private val firstSettingsSection get() = settingsSectionManager.firstSection

    val conversationNavigator = ConversationNavigator(
        navigator = navigator,
        navigationPaneState = navigationPaneState,
        extraPaneState = extraPaneState,
        onNavigateToSettings = {
            Snapshot.withMutableSnapshot {
                navStack.push(SettingsListScreen)
                navStack.push(SettingsSectionScreen(it ?: firstSettingsSection.titleKey))
            }
        },
    )

    val settingsBackStack = navigator.backStack<SettingsScreen>()

    val settingsNavigator = SettingsNavigator(
        navigator = navigator,
    )

    init {
        println("Initializing MainScreenViewModel")
    }

    override fun onCleared() {
        super.onCleared()
        println("Cleared MainScreenViewModel")
    }
}

inline fun <reified T : Any> Navigator.backStack(): Flow<List<T>> = snapshotFlow {
    val settingsStack = peekNavStack()!!
    (settingsStack.backwardItems.reversed() + settingsStack.active).filterIsInstance<T>()
}

@Serializable
sealed interface Destination : NavKey, Screen {
    @Serializable
    sealed interface Conversation : Destination {
        @Serializable
        data object List : Conversation {
            override fun toString(): String = "conversations"
        }

        @Serializable
        data object New : Conversation {
            override fun toString(): String = "conversations/new"
        }

        @Serializable
        data class Selected(val id: String) : Conversation {
            override fun toString(): String = "conversations/$id"
        }
    }
}

class ConversationNavigator(
    private val navigator: Navigator,
    val navigationPaneState: PaneState,
    val extraPaneState: PaneState,
    val onNavigateToSettings: (String?) -> Unit,
) {
    internal val isLastConversationScreen: Boolean
        get() {
            val conversationScreens = navigator.peekBackStack().filterIsInstance<Destination.Conversation>()
            return conversationScreens.size == 2
        }

    fun goBack() {
        if (!isLastConversationScreen) {
            navigator.pop()
        }
    }

    val selectedConversationId: String?
        get() {
            val conversation = navigator.peekBackStack()
                .firstOrNull { it is Destination.Conversation.Selected }
                    as Destination.Conversation.Selected?
            return conversation?.id
        }

    fun goToConversation(conversationId: String) {
        if (selectedConversationId == conversationId) return
        navigator.goTo(Destination.Conversation.Selected(conversationId))
    }

    fun goToNewConversation() {
        navigator.goTo(Destination.Conversation.New)
    }

    fun openSettings() {
        onNavigateToSettings(null)
    }

    fun openSettingsSection(titleKey: String) {
        onNavigateToSettings(titleKey)
    }
}
