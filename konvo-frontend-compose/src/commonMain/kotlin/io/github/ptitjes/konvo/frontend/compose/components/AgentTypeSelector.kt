package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

/**
 * A selector for agent types.
 *
 * @param selectedAgentType The currently selected agent type
 * @param onAgentTypeSelected Callback for when an agent type is selected
 * @param agentTypes List of available agent types
 * @param modifier The modifier to apply to this component
 */
@Composable
fun AgentTypeSelector(
    selectedAgentType: AgentType,
    onAgentTypeSelected: (AgentType) -> Unit,
    agentTypes: List<AgentType>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Agent Type",
            style = MaterialTheme.typography.titleMedium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            agentTypes.forEach { agentType ->
                FilterChip(
                    selected = selectedAgentType == agentType,
                    onClick = { onAgentTypeSelected(agentType) },
                    label = { Text(agentType.displayName) }
                )
            }
        }
    }
}

enum class AgentType(val displayName: String) {
    QuestionAnswer("Question & Answer"),
    Roleplaying("Roleplaying")
}
