package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import androidx.compose.runtime.*
import com.slack.circuit.retained.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.syrup.*
import kotlinx.coroutines.*
import kotlin.reflect.*

class AgentConfigurationPresenter(
    private val navigator: ConversationNavigator,
    private val pluginContext: PluginContext,
    private val conversationManager: ConversationManager,
) : Presenter<AgentConfigurationView.State> {
    @Composable
    override fun present(): AgentConfigurationView.State {
        val configurationPanes by pluginContext.contributions(AgentConfigurationPanes)

        val perClassPane = remember(configurationPanes) {
            configurationPanes.associateBy { it.agentConfigurationClass }
        }
        val configurationClasses = perClassPane.keys

        var selectedClass by rememberRetained { mutableStateOf(configurationClasses.first()) }

        return state(
            configurationClasses = configurationClasses,
            perClassPane = perClassPane,
            selectedClass = selectedClass,
            updateSelectedClass = { selectedClass = it }
        )
    }

    @Composable
    private fun <C : AgentConfiguration, S : AgentConfigurationState<C>> state(
        configurationClasses: Set<KClass<out AgentConfiguration>>,
        perClassPane: Map<KClass<out AgentConfiguration>, AgentConfigurationPane<*, *>>,
        selectedClass: KClass<C>,
        updateSelectedClass: (KClass<out AgentConfiguration>) -> Unit,
    ): AgentConfigurationView.State {
        val selectedPane = remember(selectedClass, perClassPane) {
            @Suppress("UNCHECKED_CAST")
            perClassPane.getValue(selectedClass) as AgentConfigurationPane<C, S>
        }

        val presenter = remember(selectedPane) { selectedPane.presenterFactory(navigator) }
        val configurationState = presenter.present()

        val scope = rememberCoroutineScope()

        return AgentConfigurationView.State(
            selectableAgentClasses = configurationClasses,
            agentLabels = { perClassPane.getValue(it).label() },
            selectedAgentClass = selectedClass,
            configurationState = configurationState,
            renderer = { state, modifier ->
                // TODO generify AgentConfigurationView.State?
                @Suppress("UNCHECKED_CAST")
                selectedPane.panel(state as S, modifier)
            },
        ) { event ->
            when (event) {
                is AgentConfigurationView.Event.SelectConfigurationClass -> {
                    updateSelectedClass(event.configurationClass)
                }

                is AgentConfigurationView.Event.CreateAgent -> {
                    check(configurationState.isValidConfiguration)
                    val configuration = configurationState.buildConfiguration()

                    scope.launch {
                        val conversation = conversationManager.newConversation(configuration)
                        navigator.goToConversation(conversation.id)
                    }
                }
            }
        }
    }
}
