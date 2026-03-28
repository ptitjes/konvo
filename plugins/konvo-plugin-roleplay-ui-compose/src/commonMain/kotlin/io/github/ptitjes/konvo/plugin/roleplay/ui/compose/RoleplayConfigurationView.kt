package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.screen.Screen
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration.*
import io.github.ptitjes.konvo.plugin.roleplay.*

data object RoleplayConfigurationView : Screen {
    sealed interface State : AgentConfigurationState<RoleplayAgentConfiguration> {
        data object Loading : State

        data class Available(
            val availableModels: List<ModelCard>,
            val availableCharacters: List<CharacterCard>,
            val availablePersonas: List<Persona>,
            val availableLorebooks: List<Lorebook>,
            val selectedCharacter: CharacterCard?,
            val selectedGreetingIndex: Int?,
            val selectedLorebook: Lorebook?,
            val selectedPersona: Persona?,
            val selectedModel: ModelCard?,
            val eventSink: (Event) -> Unit,
        ) : State

        override val isValidConfiguration: Boolean
            get() {
                if (this !is Available) return false
                return selectedCharacter != null && selectedPersona != null && selectedModel != null
            }

        override fun buildConfiguration(): RoleplayAgentConfiguration {
            check(this is Available)
            check(selectedCharacter != null && selectedPersona != null && selectedModel != null)
            return RoleplayAgentConfiguration(
                characterId = selectedCharacter.id,
                characterGreetingIndex = selectedGreetingIndex,
                personaName = selectedPersona.name,
                modelName = selectedModel.name,
                lorebookId = selectedLorebook?.id,
            )
        }

        val canCreate: Boolean
            get() {
                if (this !is Available) return false
                return selectedCharacter != null && selectedPersona != null && selectedModel != null
            }
    }

    sealed interface Event : CircuitUiEvent {
        data class SelectModel(val model: ModelCard) : Event
        data class SelectCharacter(val character: CharacterCard) : Event
        data class SelectGreetingIndex(val index: Int?) : Event
        data class SelectPersona(val persona: Persona) : Event
        data class SelectLorebook(val lorebook: Lorebook?) : Event
        data class GoToSettings(val key: String) : Event
    }
}
