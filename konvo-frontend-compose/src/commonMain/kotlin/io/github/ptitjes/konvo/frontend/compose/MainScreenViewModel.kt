package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.*
import androidx.lifecycle.*
import androidx.navigation3.runtime.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.*
import kotlinx.serialization.*

/**
 * ViewModel managing the current high-level application navigation state.
 */
class MainScreenViewModel : ViewModel() {

    private val _backStack = NavBackStack<Destination>(
        Destination.Conversation.List,
        Destination.Conversation.New,
    )
    val backStack: List<Destination> by derivedStateOf { _backStack.toList() }

    val navigationPaneState = PaneState()
    val extraPaneState = PaneState()

    val navigator = Navigator(
        backStack = _backStack,
        navigationPaneState = navigationPaneState,
        extraPaneState = extraPaneState,
    )

    init {
        println("Initializing MainScreenViewModel")
    }

    override fun onCleared() {
        super.onCleared()
        println("Cleared MainScreenViewModel")
    }
}

// TODO remove and update translation strings
/**
 * High-level application navigation destinations.
 */
enum class MainDestination(
    val icon: ImageVector,
) {
    Conversations(icon = Icons.AutoMirrored.Filled.Chat),
    Archive(icon = Icons.Filled.Archive),
    KnowledgeBases(icon = Icons.Filled.Dataset),
    Settings(icon = Icons.Filled.Settings),
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

    // TODO move settings into their own NavDisplay/Backstack (inside a dialog)
    @Serializable
    sealed interface Setting : Destination {
        @Serializable
        data object List : Setting {
            override fun toString(): String = "settings"
        }

        @Serializable
        data class Section(val key: String) : Setting {
            override fun toString(): String = "settings/$key"
        }
    }
}

class Navigator(
    val backStack: NavBackStack<Destination>,
    val navigationPaneState: PaneState,
    val extraPaneState: PaneState,
) {
    val navigationExpanded: Boolean get() = navigationPaneState.targetValue.isExpanded
    val extraExpanded: Boolean get() = extraPaneState.targetValue.isExpanded

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

    fun navigateToSettingSection(titleKey: String) {
        backStack.navigate(Destination.Setting.Section(titleKey), popUpTo = Destination.Setting.List)
    }

    val selectedSettingSectionKey: String?
        get() = (backStack.lastOrNull() as? Destination.Setting.Section)?.key
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
