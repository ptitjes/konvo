package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import androidx.compose.runtime.*
import com.slack.circuit.retained.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.settings.*

class RoleplayConfigurationPresenter(
    private val modelManager: ModelManager,
    private val characterManager: CharacterManager,
    private val lorebookManager: LorebookManager,
    private val settingsRepository: SettingsRepository,
) : Presenter<RoleplayConfigurationView.State> {
    @Composable
    override fun present(): RoleplayConfigurationView.State {
        val models by modelManager.models.collectAsState(null)
        val characters by characterManager.characters.collectAsState(null)
        val lorebooks by lorebookManager.lorebooks.collectAsState(null)

        val availableModels = models
        val availableCharacters = characters
        val availableLorebooks = lorebooks

        return if (availableModels == null || availableCharacters == null || availableLorebooks == null) {
            RoleplayConfigurationView.State.Loading
        } else {
            val roleplaySettings by settingsRepository.getSettings(RoleplaySettingsKey).collectAsState()
            val defaultModelName = roleplaySettings.defaultPreferredModelName
            val defaultPersonaName = roleplaySettings.defaultPersonaName

            val preferredModel = defaultModelName?.let { name ->
                availableModels.firstOrNull { it.name == name }
            }

            val personaSettings by settingsRepository.getSettings(PersonaSettingsKey).collectAsState()
            val preferredPersona = defaultPersonaName?.let { name ->
                personaSettings.personas.firstOrNull { it.name == name }
            } ?: personaSettings.personas.firstOrNull()

            var selectedCharacter by rememberRetained { mutableStateOf(availableCharacters.firstOrNull()) }
            var selectedGreetingIndex by rememberRetained { mutableStateOf<Int?>(null) }
            var selectedLorebook by rememberRetained { mutableStateOf(availableLorebooks.firstOrNull()) }
            var selectedPersona by rememberRetained { mutableStateOf(preferredPersona) }
            var selectedModel by rememberRetained { mutableStateOf(preferredModel) }

            RoleplayConfigurationView.State.Available(
                availableModels = availableModels,
                availableCharacters = availableCharacters,
                availableLorebooks = availableLorebooks,
                availablePersonas = personaSettings.personas,
                selectedCharacter = selectedCharacter,
                selectedGreetingIndex = selectedGreetingIndex,
                selectedLorebook = selectedLorebook,
                selectedPersona = selectedPersona,
                selectedModel = selectedModel,
            ) { event ->
                when (event) {
                    is RoleplayConfigurationView.Event.SelectCharacter -> {
                        if (selectedCharacter?.id != event.character.id) {
                            selectedCharacter = event.character
                            selectedGreetingIndex = null
                        }
                    }

                    is RoleplayConfigurationView.Event.SelectGreetingIndex -> selectedGreetingIndex = event.index
                    is RoleplayConfigurationView.Event.SelectLorebook -> selectedLorebook = event.lorebook
                    is RoleplayConfigurationView.Event.SelectModel -> selectedModel = event.model
                    is RoleplayConfigurationView.Event.SelectPersona -> selectedPersona = event.persona
                }
            }
        }
    }
}