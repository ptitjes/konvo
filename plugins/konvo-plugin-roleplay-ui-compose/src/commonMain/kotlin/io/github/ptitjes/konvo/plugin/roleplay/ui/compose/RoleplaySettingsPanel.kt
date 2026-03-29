package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*

internal data object RoleplaySettingsView {
    data class State(
        val settings: RoleplaySettings,
        val models: List<ModelCard>,
        val personas: List<Persona>,
        val eventSink: (Event) -> Unit,
    ) : SettingsSectionState

    sealed interface Event : CircuitUiEvent {
        data class UpdateSettings(val settings: RoleplaySettings) : Event
        data class GoToSettings(val key: String) : Event
    }
}

internal class RoleplaySettingsPresenter(
    private val navigator: SettingsNavigator,
    private val settingsRepository: SettingsRepository,
    private val modelManager: ModelManager,
) : Presenter<RoleplaySettingsView.State> {
    @Composable
    override fun present(): RoleplaySettingsView.State {
        var settings by settingsRepository.mutableSettingsOf(RoleplaySettingsKey)
        val models by modelManager.models.collectAsState(initial = emptyList())

        val personaSettings by settingsRepository.getSettings(PersonaSettingsKey).collectAsState()
        val personas = personaSettings.personas

        return RoleplaySettingsView.State(
            settings = settings,
            models = models,
            personas = personas,
        ) { event ->
            when (event) {
                is RoleplaySettingsView.Event.UpdateSettings -> settings = event.settings
                is RoleplaySettingsView.Event.GoToSettings -> navigator.goToSection(event.key)
            }
        }
    }
}

@Composable
internal fun RoleplaySettingsPanel(state: RoleplaySettingsView.State) {
    val settings = state.settings
    val models = state.models
    val personaSettings = state.personas

    // Default user persona
    SettingsBox(
        title = i18n.roleplay.defaultPersonaTitle,
        description = i18n.roleplay.defaultPersonaDescription,
        bottomContent = {
            if (personaSettings.isEmpty()) {
                OutlineBox(modifier = Modifier.fillMaxWidth()) {
                    UnavailabilityPlaceholder(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        unavailabilityText = i18n.roleplay.noPersonaDefined,
                    ) {
                        state.eventSink(RoleplaySettingsView.Event.GoToSettings("personas"))
                    }
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
                        state.eventSink(
                            RoleplaySettingsView.Event.UpdateSettings(
                                settings.copy(defaultPersonaName = persona.name)
                            )
                        )
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
            val selectedModel = remember(settings.defaultPreferredModelName, models) {
                settings.defaultPreferredModelName?.let { name ->
                    models.firstOrNull { it.name == name }
                } ?: models.firstOrNull()
            }

            ModelSelector(
                modifier = Modifier.fillMaxWidth(),
                label = null,
                selectedModel = selectedModel,
                onModelSelected = { model ->
                    state.eventSink(
                        RoleplaySettingsView.Event.UpdateSettings(
                            settings.copy(defaultPreferredModelName = model.name)
                        )
                    )
                },
                models = models,
                onOpenModelSettings = {
                    state.eventSink(RoleplaySettingsView.Event.GoToSettings("models"))
                },
            )
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
                    state.eventSink(
                        RoleplaySettingsView.Event.UpdateSettings(
                            settings.copy(defaultSystemPrompt = newValue)
                        )
                    )
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
                            state.eventSink(
                                RoleplaySettingsView.Event.UpdateSettings(
                                    settings.copy(defaultScanDepth = value)
                                )
                            )
                        },
                        label = i18n.roleplay.scanDepthLabel,
                    )

                    OutlinedIntegerField(
                        modifier = Modifier.weight(1f),
                        value = settings.defaultTokenBudget,
                        onValueChange = { value ->
                            state.eventSink(
                                RoleplaySettingsView.Event.UpdateSettings(
                                    settings.copy(defaultTokenBudget = value)
                                )
                            )
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
                            state.eventSink(
                                RoleplaySettingsView.Event.UpdateSettings(
                                    settings.copy(defaultRecursiveScanning = checked)
                                )
                            )
                        },
                    )
                }
            }
        }
    )
}
