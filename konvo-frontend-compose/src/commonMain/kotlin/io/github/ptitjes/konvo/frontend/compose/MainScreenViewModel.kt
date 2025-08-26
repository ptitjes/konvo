package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.*
import androidx.compose.ui.graphics.vector.*
import androidx.lifecycle.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.*
import kotlinx.serialization.*

/**
 * ViewModel managing the current high-level application navigation state.
 */
class MainScreenViewModel : ViewModel() {

    private val _backStack = mutableStateListOf<Destination>(
        Destination.Conversation.List,
        Destination.Conversation.New,
    )
    val backStack: List<Destination> by derivedStateOf { _backStack.toList() }

    val navigator = Navigator(backStack = _backStack)

    init {
        println("Initializing MainScreenViewModel")
    }

    override fun onCleared() {
        super.onCleared()
        println("Cleared MainScreenViewModel")
    }
}

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
    val mainDestination: MainDestination

    @Serializable
    sealed interface Conversation : Destination {
        override val mainDestination: MainDestination
            get() = MainDestination.Conversations

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

    @Serializable
    data object Archive : Destination {
        override val mainDestination: MainDestination
            get() = MainDestination.Archive
    }

    @Serializable
    data object KnowledgeBase : Destination {
        override val mainDestination: MainDestination
            get() = MainDestination.KnowledgeBases
    }

    @Serializable
    sealed interface Setting : Destination {
        override val mainDestination: MainDestination
            get() = MainDestination.Settings

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

class Navigator(val backStack: SnapshotStateList<Destination>) {
    fun navigateTo(mainDestination: MainDestination) {
        when (mainDestination) {
            MainDestination.Conversations -> backStack.navigate(Destination.Conversation.List, clear = true)
            MainDestination.Archive -> backStack.navigate(Destination.Archive, clear = true)
            MainDestination.KnowledgeBases -> backStack.navigate(Destination.KnowledgeBase, clear = true)
            MainDestination.Settings -> backStack.navigate(Destination.Setting.List, clear = true)
        }
    }

    fun isInMainDestination(mainDestination: MainDestination): Boolean {
        return backStack.lastOrNull()?.mainDestination == mainDestination
    }

    fun navigateBack() {
        backStack.removeLastOrNull()
    }

    fun navigateToConversation(conversationId: String) {
        backStack.navigate(Destination.Conversation.Selected(conversationId), popUpTo = Destination.Conversation.List)
    }

    fun navigateToNewConversation() {
        backStack.navigate(Destination.Conversation.New, popUpTo = Destination.Conversation.List)
    }

    fun navigateToSettingSection(titleKey: String) {
        backStack.navigate(Destination.Setting.Section(titleKey), popUpTo = Destination.Setting.List)
    }
}

private fun <T : NavKey> SnapshotStateList<T>.navigate(
    destination: T,
    clear: Boolean = false,
    popUpTo: T? = null,
    inclusive: Boolean = false,
) {
    if (clear) clear()
    if (popUpTo != null) {
        val index = indexOfLast { it == popUpTo }
        if (index >= 0) {
            removeRange(index + if (inclusive) 0 else 1, size)
        }
    }
    add(destination)
}
