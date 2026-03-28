package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.type.*
import kotlin.reflect.*

object AgentConfigurationPanes : ExtensionPoint.Plural<AgentConfigurationPane<*, *>>(generic())

data class AgentConfigurationPane<C : AgentConfiguration, S : AgentConfigurationState<C>>(
    val agentConfigurationClass: KClass<out C>,
    val label: @Composable () -> String,
    val presenterFactory: (navigator: Navigator) -> Presenter<out S>,
    val panel: @Composable (S, Modifier) -> Unit,
) {
    companion object {
        inline operator fun <reified C : AgentConfiguration, S : AgentConfigurationState<C>> invoke(
            noinline label: @Composable () -> String,
            noinline presenterFactory: (navigator: Navigator) -> Presenter<out S>,
            noinline panel: @Composable (S, Modifier) -> Unit,
        ): AgentConfigurationPane<C, S> {
            return AgentConfigurationPane(
                agentConfigurationClass = C::class,
                label = label,
                presenterFactory = presenterFactory,
                panel = panel,
            )
        }
    }
}
