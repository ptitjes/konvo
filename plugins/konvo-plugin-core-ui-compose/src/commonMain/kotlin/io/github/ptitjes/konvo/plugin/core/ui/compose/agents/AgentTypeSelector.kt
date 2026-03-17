package io.github.ptitjes.konvo.plugin.core.ui.compose.agents

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*

/**
 * A selector for agent types.
 *
 * @param selectedAgentType The currently selected agent type
 * @param onSelectAgentType Callback for when an agent type is selected
 * @param agentTypes List of available agent types
 * @param modifier The modifier to apply to this component
 */
@Composable
fun AgentTypeSelector(
    selectedAgentType: AgentType,
    onSelectAgentType: (AgentType) -> Unit,
    agentTypes: List<AgentType>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            agentTypes.forEach { agentType ->
                ElevatedFilterChip(
                    selected = selectedAgentType == agentType,
                    onClick = { onSelectAgentType(agentType) },
                    label = { Text(i18n.agents.agentTypeDisplayName(agentType)) },
                )
            }
        }
    }
}

enum class AgentType {
    QuestionAnswer,
    Roleplay,
}
