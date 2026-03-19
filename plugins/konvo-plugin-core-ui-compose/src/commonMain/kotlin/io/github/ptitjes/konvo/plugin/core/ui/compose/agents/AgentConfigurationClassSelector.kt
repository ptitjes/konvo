package io.github.ptitjes.konvo.plugin.core.ui.compose.agents

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.agents.*
import kotlin.reflect.*

/**
 * A selector for agent types.
 *
 * @param selectedConfigurationClass The currently selected agent configuration class
 * @param onSelectConfigurationClass Callback for when an agent type is selected
 * @param agentConfigurationClasses List of available agent types
 * @param modifier The modifier to apply to this component
 */
@Composable
fun AgentConfigurationClassSelector(
    selectedConfigurationClass: KClass<out AgentConfiguration>,
    onSelectConfigurationClass: (KClass<out AgentConfiguration>) -> Unit,
    agentConfigurationClasses: Set<KClass<out AgentConfiguration>>,
    agentLabels: @Composable (KClass<out AgentConfiguration>) -> String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            agentConfigurationClasses.forEach { configurationClass ->
                ElevatedFilterChip(
                    selected = selectedConfigurationClass == configurationClass,
                    onClick = { onSelectConfigurationClass(configurationClass) },
                    label = { Text(agentLabels(configurationClass)) },
                )
            }
        }
    }
}
