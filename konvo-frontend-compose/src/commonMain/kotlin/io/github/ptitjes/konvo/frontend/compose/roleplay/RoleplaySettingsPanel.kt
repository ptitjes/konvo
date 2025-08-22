package io.github.ptitjes.konvo.frontend.compose.roleplay

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.frontend.compose.models.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
import org.kodein.di.compose.*

@Composable
fun RoleplaySettingsPanel(
    settings: RoleplaySettings,
    updateSettings: (updater: (previous: RoleplaySettings) -> RoleplaySettings) -> Unit,
) {
    // We need models to offer a selector for the default preferred model
    val modelManager by rememberInstance<ModelManager>()
    val models by modelManager.models.collectAsState(initial = emptyList())

    // Default user persona
    SettingsBox(
        title = strings.roleplay.defaultPersonaTitle,
        description = strings.roleplay.defaultPersonaDescription,
        bottomContent = {
            val personaSettings by rememberSetting(PersonaSettingsKey, emptyList()) { it.personas }
            if (personaSettings.isEmpty()) {
                Text(
                    text = strings.roleplay.noPersonaDefined,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                val selectedPersona = remember(settings.defaultPersonaName, personaSettings) {
                    personaSettings.firstOrNull { it.name == settings.defaultPersonaName } ?: personaSettings.first()
                }
                PersonaSelector(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    label = null,
                    selectedPersona = selectedPersona,
                    onPersonaSelected = { persona ->
                        updateSettings { previous -> previous.copy(defaultPersonaName = persona.name) }
                    },
                    personas = personaSettings,
                )
            }
        }
    )

    // Default preferred model selector
    SettingsBox(
        title = strings.roleplay.defaultPreferredModelTitle,
        description = strings.roleplay.defaultPreferredModelDescription,
        bottomContent = {
            if (models.isEmpty()) {
                Text(
                    text = strings.roleplay.noAvailableModels,
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
        title = strings.roleplay.defaultSystemPromptTitle,
        description = strings.roleplay.defaultSystemPromptDescription,
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
        title = strings.roleplay.defaultLorebookSettingsTitle,
        description = strings.roleplay.defaultLorebookSettingsDescription,
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
                        label = strings.roleplay.scanDepthLabel,
                    )

                    OutlinedIntegerField(
                        modifier = Modifier.weight(1f),
                        value = settings.defaultTokenBudget,
                        onValueChange = { value ->
                            updateSettings { previous -> previous.copy(defaultTokenBudget = value) }
                        },
                        label = strings.roleplay.tokenBudgetLabel,
                    )
                }

                // Recursive scanning switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = strings.roleplay.recursiveScanningLabel,
                    )
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
