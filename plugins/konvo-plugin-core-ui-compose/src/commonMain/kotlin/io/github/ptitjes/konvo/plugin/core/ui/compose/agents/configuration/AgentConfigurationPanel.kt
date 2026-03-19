package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.*

@Composable
internal fun AgentConfigurationPanel(
    state: AgentConfigurationView.State,
    onGoToSettingsClick: (titleKey: String) -> Unit,
) {
    Column {
        AgentConfigurationClassSelector(
            selectedConfigurationClass = state.selectedAgentClass,
            onSelectConfigurationClass = { state.eventSink(AgentConfigurationView.Event.SelectConfigurationClass(it)) },
            agentConfigurationClasses = state.selectableAgentClasses,
            agentLabels = { state.agentLabels(it) },
        )

        key(state.selectedAgentClass) {
            state.renderer(state.configurationState, Modifier.weight(1f))
        }
    }
}
