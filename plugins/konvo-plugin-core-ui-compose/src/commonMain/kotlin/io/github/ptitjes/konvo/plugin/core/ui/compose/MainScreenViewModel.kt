package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.navigation3.runtime.*
import com.slack.circuit.runtime.screen.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import io.github.ptitjes.syrup.*
import kotlinx.serialization.*

/**
 * ViewModel managing the current high-level application navigation state.
 */
class MainScreenViewModel(
    pluginContext: PluginContext,
) : ViewModel() {

    private val _backStack = NavBackStack<Destination>(
        Destination.Conversation.List,
        Destination.Conversation.New,
    )
    val backStack: List<Destination> by derivedStateOf { _backStack.toList() }

    val navigationPaneState = PaneState.Companion()
    val extraPaneState = PaneState.Companion()

    private val settingsSections by pluginContext.contributions(SettingsSections)
    private val firstSettingsSection = settingsSections.toList()
        .recursivelySortedBy { it.titleKey }.first()

    val navigator = Navigator(
        backStack = _backStack,
        navigationPaneState = navigationPaneState,
        extraPaneState = extraPaneState,
        onNavigateToSettings = {
            _settingsBackStack.addAll(
                listOf(
                    SettingsDestination.List,
                    SettingsDestination.Section(it ?: firstSettingsSection.titleKey)
                )
            )
        },
    )

    private val _settingsBackStack = NavBackStack<SettingsDestination>()

    val settingsBackStack: List<SettingsDestination> by derivedStateOf { _settingsBackStack.toList() }

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

@Serializable
sealed interface SettingsDestination : NavKey, Screen {
    @Serializable
    data object List : SettingsDestination {
        override fun toString(): String = "settings"
    }

    @Serializable
    data class Section(val key: String) : SettingsDestination {
        override fun toString(): String = "settings/$key"
    }
}

class SettingsNavigator(
    val backStack: NavBackStack<SettingsDestination>,
) {
    val isLastSettingsSection: Boolean
        get() =
            backStack.size == 2

    fun closeSettings() {
        backStack.clear()
    }

    fun navigateBack() {
        backStack.removeLastOrNull()
    }

    fun navigateToSettingSection(titleKey: String) {
        backStack.add(SettingsDestination.Section(titleKey))
    }

    val selectedSettingSectionKey: String?
        get() = (backStack.lastOrNull() as? SettingsDestination.Section)?.key
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
