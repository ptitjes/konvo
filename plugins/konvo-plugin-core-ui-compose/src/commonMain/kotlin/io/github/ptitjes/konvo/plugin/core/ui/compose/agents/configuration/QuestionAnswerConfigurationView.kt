package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.screen.Screen
import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.models.*

data object QuestionAnswerConfigurationView : Screen {
    sealed interface State : AgentConfigurationState<QuestionAnswerAgentConfiguration> {
        data object Loading : State

        data class Available(
            val availableModels: List<ModelCard>,
            val availableMcpServers: Set<String>,
            val selectedModel: ModelCard?,
            val selectedMcpServers: Set<String>,
            val eventSink: (Event) -> Unit,
        ) : State

        override val isValidConfiguration: Boolean
            get() {
                if (this !is Available) return false
                return selectedModel != null
            }

        override fun buildConfiguration(): QuestionAnswerAgentConfiguration {
            check(this is Available)
            check(selectedModel != null)
            return QuestionAnswerAgentConfiguration(
                modelName = selectedModel.name,
                mcpServerNames = selectedMcpServers,
            )
        }
    }

    sealed interface Event : CircuitUiEvent {
        data class SelectModel(val model: ModelCard) : Event
        data class SelectMcpServerNames(val mcpServerNames: Set<String>) : Event
    }
}
