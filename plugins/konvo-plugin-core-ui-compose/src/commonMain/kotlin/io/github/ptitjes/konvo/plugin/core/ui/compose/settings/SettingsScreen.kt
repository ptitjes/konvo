package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.navigation3.runtime.*
import com.slack.circuit.runtime.screen.*
import kotlinx.serialization.*

@Serializable
sealed interface SettingsScreen : NavKey, Screen
