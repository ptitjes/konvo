package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.frontend.compose.components.settings.*

/**
 * Settings panel for character-related preferences.
 */
@Composable
fun CharacterSettingsPanel(
    settings: CharacterSettings,
    updateSettings: ((CharacterSettings) -> CharacterSettings) -> Unit,
) {
    var text by remember(settings.filteredTags) {
        mutableStateOf(settings.filteredTags.joinToString(separator = ", "))
    }

    // Keep local text in sync if settings are externally updated
    LaunchedEffect(settings.filteredTags) {
        val joined = settings.filteredTags.joinToString(separator = ", ")
        if (joined != text) {
            text = joined
        }
    }

    SettingsBox(
        title = "Character tags filter",
        description = "Tags listed here will be excluded when showing characters. Separate tags with commas.",
        bottomContent = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                value = text,
                onValueChange = { newValue ->
                    text = newValue
                    val parsed = newValue.split(',')
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                    updateSettings { previous -> previous.copy(filteredTags = parsed) }
                },
                singleLine = true,
                placeholder = { Text("e.g. nsfw, beta, wip") },
            )
        }
    )
}
