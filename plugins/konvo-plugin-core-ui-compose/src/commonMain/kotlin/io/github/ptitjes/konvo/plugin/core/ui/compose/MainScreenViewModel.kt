package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.navigation3.runtime.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import kotlinx.serialization.*

/**
 * ViewModel managing the current high-level application navigation state.
 */
internal class MainScreenViewModel(
    private val settingsSectionManager: SettingsSectionManager,
) : ViewModel() {

    private val _backStack = NavBackStack<Destination>(
        Destination.Conversation.List,
        Destination.Conversation.New,
    )
    val backStack: List<Destination> by derivedStateOf { _backStack.toList() }

    val navigationPaneState = PaneState.Companion()
    val extraPaneState = PaneState.Companion()

    private val firstSettingsSection get() = settingsSectionManager.firstSection

    val navigator = Navigator(
        backStack = _backStack,
        navigationPaneState = navigationPaneState,
        extraPaneState = extraPaneState,
        onNavigateToSettings = {
            _settingsBackStack.addAll(
                listOf(
                    SettingsListScreen,
                    SettingsSectionScreen(it ?: firstSettingsSection.titleKey)
                )
            )
        },
    )

    private val _settingsBackStack = NavBackStack<SettingsScreen>()

    val settingsBackStack: List<SettingsScreen> by derivedStateOf { _settingsBackStack.toList() }

    val settingsNavigator = SettingsNavigator(
        backStack = _settingsBackStack,
    )

    init {
        println("Initializing MainScreenViewModel")
    }

    override fun onCleared() {
        super.onCleared()
        println("Cleared MainScreenViewModel")
    }
}

@Serializable
sealed interface Destination : NavKey {
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

class Navigator(
    val backStack: NavBackStack<Destination>,
    val navigationPaneState: PaneState,
    val extraPaneState: PaneState,
    val onNavigateToSettings: (String?) -> Unit,
) {
    fun navigateBack() {
        backStack.removeLastOrNull()
    }

    val selectedConversationId: String?
        get() = (backStack.lastOrNull() as? Destination.Conversation.Selected)?.id

    fun navigateToConversation(conversationId: String) {
        if (selectedConversationId == conversationId) return
        backStack.navigate(Destination.Conversation.Selected(conversationId))
    }

    fun navigateToNewConversation() {
        backStack.navigate(Destination.Conversation.New)
    }

    fun openSettings() {
        onNavigateToSettings(null)
    }

    fun openSettingsSection(titleKey: String) {
        onNavigateToSettings(titleKey)
    }
}

private fun <T : NavKey> NavBackStack<T>.navigate(
    destination: T,
    clear: Boolean = false,
    popUpTo: T? = null,
    inclusive: Boolean = false,
) {
    if (clear) clear()
    if (popUpTo != null) {
        val index = indexOfLast { it == popUpTo }
        if (index >= 0) {
            dropLast(size - index + if (inclusive) 0 else 1)
        }
    }
    add(destination)
}
