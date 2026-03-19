package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import org.kodein.di.compose.*

@Composable
fun RoleplaySettingsPanel() {
    var settings by rememberMutableSettings(RoleplaySettingsKey)

    // We need models to offer a selector for the default preferred model
    val modelManager by rememberInstance<ModelManager>()
    val models by modelManager.models.collectAsState(initial = emptyList())

    // Default user persona
    SettingsBox(
        title = i18n.roleplay.defaultPersonaTitle,
        description = i18n.roleplay.defaultPersonaDescription,
        bottomContent = {
            val personaSettings by rememberSetting(PersonaSettingsKey, emptyList()) { it.personas }
            if (personaSettings.isEmpty()) {
                OutlineBox(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = i18n.roleplay.noPersonaDefined,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            } else {
                val selectedPersona = remember(settings.defaultPersonaName, personaSettings) {
                    personaSettings.firstOrNull { it.name == settings.defaultPersonaName } ?: personaSettings.first()
                }
                PersonaSelector(
                    modifier = Modifier.fillMaxWidth(),
                    label = null,
                    selectedPersona = selectedPersona,
                    onPersonaSelected = { persona ->
                        settings = settings.copy(defaultPersonaName = persona.name)
                    },
                    personas = personaSettings,
                )
            }
        }
    )

    // Default preferred model selector
    SettingsBox(
        title = i18n.roleplay.defaultPreferredModelTitle,
        description = i18n.roleplay.defaultPreferredModelDescription,
        bottomContent = {
            if (models.isEmpty()) {
                OutlineBox(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = i18n.roleplay.noAvailableModels,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(8.dp),
                    )
                }
            } else {
                val selectedModel = remember(settings.defaultPreferredModelName, models) {
                    settings.defaultPreferredModelName?.let { name ->
                        models.firstOrNull { it.name == name }
                    } ?: models.first()
                }

                ModelSelector(
                    modifier = Modifier.fillMaxWidth(),
                    label = null,
                    selectedModel = selectedModel,
                    onModelSelected = { model ->
                        settings = settings.copy(defaultPreferredModelName = model.name)
                    },
                    models = models,
                )
            }
        }
    )

    // Default system prompt
    SettingsBox(
        title = i18n.roleplay.defaultSystemPromptTitle,
        description = i18n.roleplay.defaultSystemPromptDescription,
        bottomContent = {
            OutlinedTextField(
                label = {},
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                value = settings.defaultSystemPrompt,
                onValueChange = { newValue ->
                    settings = settings.copy(defaultSystemPrompt = newValue)
                },
            )
        }
    )

    // Default lorebook settings
    SettingsBox(
        title = i18n.roleplay.defaultLorebookSettingsTitle,
        description = i18n.roleplay.defaultLorebookSettingsDescription,
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
                            settings = settings.copy(defaultScanDepth = value)
                        },
                        label = i18n.roleplay.scanDepthLabel,
                    )

                    OutlinedIntegerField(
                        modifier = Modifier.weight(1f),
                        value = settings.defaultTokenBudget,
                        onValueChange = { value ->
                            settings = settings.copy(defaultTokenBudget = value)
                        },
                        label = i18n.roleplay.tokenBudgetLabel,
                    )
                }

                // Recursive scanning switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = i18n.roleplay.recursiveScanningLabel,
                    )
                    Switch(
                        checked = settings.defaultRecursiveScanning,
                        onCheckedChange = { checked ->
                            settings = settings.copy(defaultRecursiveScanning = checked)
                        },
                    )
                }
            }
        }
    )
}
