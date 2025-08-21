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
fun RoleplaySettingsPanel(
    settings: RoleplaySettings,
    updateSettings: (updater: (previous: RoleplaySettings) -> RoleplaySettings) -> Unit,
) {
    // We need models to offer a selector for the default preferred model
    val modelManager by rememberInstance<ModelManager>()
    val models by modelManager.models.collectAsState(initial = emptyList())

    // Default user persona name
    SettingsBox(
        title = "Default user persona",
        description = "Name used for the user's persona in new roleplay conversations.",
        bottomContent = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                value = settings.defaultUserPersonaName,
                onValueChange = { newValue ->
                    updateSettings { previous -> previous.copy(defaultUserPersonaName = newValue) }
                },
                singleLine = true,
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
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    label = null,
                    selectedModel = selectedModel,
                    onModelSelected = { model ->
                        updateSettings { previous -> previous.copy(defaultPreferredModelName = model.name) }
                    },
                    models = models,
                )
            }
        }
    )

    // Default system prompt
    SettingsBox(
        title = "Default system prompt",
        description = "Used when the character card doesn't define its own system prompt.",
        bottomContent = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).heightIn(min = 120.dp),
                value = settings.defaultSystemPrompt,
                onValueChange = { newValue ->
                    updateSettings { previous -> previous.copy(defaultSystemPrompt = newValue) }
                },
            )
        }
    )

    // Default lorebook settings
    SettingsBox(
        title = "Default Lorebook settings",
        description = "Used when the character card doesn't define its own lorebook configuration.",
        bottomContent = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Numeric fields for scan depth and token budget
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedIntegerField(
                        modifier = Modifier.weight(1f),
                        value = settings.defaultScanDepth,
                        onValueChange = { value ->
                            updateSettings { previous -> previous.copy(defaultScanDepth = value) }
                        },
                        label = "Scan depth",
                    )

                    OutlinedIntegerField(
                        modifier = Modifier.weight(1f),
                        value = settings.defaultTokenBudget,
                        onValueChange = { value ->
                            updateSettings { previous -> previous.copy(defaultTokenBudget = value) }
                        },
                        label = "Token budget",
                    )
                }

                // Recursive scanning switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = "Recursive scanning")
                    Switch(
                        checked = settings.defaultRecursiveScanning,
                        onCheckedChange = { checked ->
                            updateSettings { previous -> previous.copy(defaultRecursiveScanning = checked) }
                        },
                    )
                }
            }
        }
    )
}
