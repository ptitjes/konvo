package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.screen.Screen
import io.github.ptitjes.konvo.plugin.core.agents.*
import kotlin.reflect.*

data object AgentConfigurationView : Screen {
    data class State(
        val selectableAgentClasses: Set<KClass<out AgentConfiguration>>,
        val agentLabels: @Composable (KClass<out AgentConfiguration>) -> String,
        val selectedAgentClass: KClass<out AgentConfiguration>,
        val configurationState: AgentConfigurationState<AgentConfiguration>,
        val renderer: @Composable (AgentConfigurationState<AgentConfiguration>, Modifier) -> Unit,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    sealed interface Event : CircuitUiEvent {
        data class SelectConfigurationClass(val configurationClass: KClass<out AgentConfiguration>) : Event
        data object CreateAgent : Event
    }
}

interface AgentConfigurationState<out C : AgentConfiguration> : CircuitUiState {
    val isValidConfiguration: Boolean
    fun buildConfiguration(): C
}
