package io.github.ptitjes.konvo.frontend.compose.viewmodels

import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.*
import androidx.lifecycle.*

/**
 * ViewModel managing the current high-level application navigation state.
 */
class AppViewModel : ViewModel() {
    var state: AppState by mutableStateOf(AppState.Conversations)
        private set

    fun select(state: AppState) {
        this.state = state
    }
}

/**
 * High-level application navigation destinations.
 */
enum class AppState(
    val icon: ImageVector,
    val contentDescription: String,
) {
    Conversations(
        icon = Icons.AutoMirrored.Filled.Chat,
        contentDescription = "Conversations",
    ),
    Archive(
        icon = Icons.Filled.Archive,
        contentDescription = "Archive",
    ),
    KnowledgeBases(
        icon = Icons.Filled.Dataset,
        contentDescription = "Knowledge Bases",
    ),
    Settings(
        icon = Icons.Filled.Settings,
        contentDescription = "Settings",
    ),
}
