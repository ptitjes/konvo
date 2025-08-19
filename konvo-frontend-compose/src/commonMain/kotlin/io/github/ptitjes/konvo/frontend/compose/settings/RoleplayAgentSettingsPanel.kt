package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.frontend.compose.components.*
import io.github.ptitjes.konvo.frontend.compose.components.settings.*
import org.kodein.di.compose.*

@Composable
fun RoleplayAgentSettingsPanel(
    settings: RoleplayAgentSettings,
    updateSettings: (updater: (previous: RoleplayAgentSettings) -> RoleplayAgentSettings) -> Unit,
) {
    // We need models to offer a selector for the default preferred model
    val modelManager by rememberInstance<ModelManager>()
    val models by modelManager.models.collectAsState(initial = emptyList())

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // Default user persona name
        SettingsBox(
            title = "Default user persona",
            description = "Name used for the user's persona in new roleplay conversations.",
            bottomContent = {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = settings.defaultUserPersonaName,
                    onValueChange = { newValue ->
                        updateSettings { previous -> previous.copy(defaultUserPersonaName = newValue) }
                    },
                    singleLine = true,
                    label = { Text("User persona name") },
                )
            }
        )

        // Default preferred model selector
        SettingsBox(
            title = "Default preferred model",
            description = "Model used by default for new roleplay conversations.",
            bottomContent = {
                if (models.isEmpty()) {
                    Text(
                        text = "No available models",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                    )
                } else {
                    val selectedModel = remember(settings.defaultPreferredModelName, models) {
                        settings.defaultPreferredModelName?.let { name ->
                            models.firstOrNull { it.name == name }
                        } ?: models.first()
                    }

                    ModelSelector(
                        selectedModel = selectedModel,
                        onModelSelected = { model ->
                            updateSettings { previous -> previous.copy(defaultPreferredModelName = model.name) }
                        },
                        models = models,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        )

        // Default system prompt
        SettingsBox(
            title = "Default system prompt",
            description = "Used when the character card doesn't define its own system prompt.",
            bottomContent = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                        value = settings.defaultSystemPrompt,
                        onValueChange = { newValue ->
                            updateSettings { previous -> previous.copy(defaultSystemPrompt = newValue) }
                        },
                        label = { Text("Default system prompt") },
                    )
                }
            }
        )
    }
}
