package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.slack.circuit.retained.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.screen.Screen
import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import io.github.ptitjes.syrup.*
import kotlinx.coroutines.*
import org.jetbrains.compose.resources.*
import kotlin.reflect.*

data object AgentConfigurationScreen : Screen {
    internal data class State(
        val selectableAgentClasses: Set<KClass<out AgentConfiguration>>,
        val agentLabels: @Composable (KClass<out AgentConfiguration>) -> String,
        val selectedAgentClass: KClass<out AgentConfiguration>,
        val configurationState: AgentConfigurationState<AgentConfiguration>,
        val renderer: @Composable (AgentConfigurationState<AgentConfiguration>, Modifier) -> Unit,
        val eventSink: (Event) -> Unit,
    ) : CircuitUiState

    internal sealed interface Event : CircuitUiEvent {
        data class SelectConfigurationClass(val configurationClass: KClass<out AgentConfiguration>) : Event
        data object CreateAgent : Event
    }
}

interface AgentConfigurationState<out C : AgentConfiguration> : CircuitUiState {
    val isValidConfiguration: Boolean
    fun buildConfiguration(): C
}

internal class AgentConfigurationPresenter(
    private val navigator: ConversationNavigator,
    private val pluginContext: PluginContext,
    private val conversationManager: ConversationManager,
) : Presenter<AgentConfigurationScreen.State> {
    @Composable
    override fun present(): AgentConfigurationScreen.State {
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
    ): AgentConfigurationScreen.State {
        val selectedPane = remember(selectedClass, perClassPane) {
            @Suppress("UNCHECKED_CAST")
            perClassPane.getValue(selectedClass) as AgentConfigurationPane<C, S>
        }

        val presenter = remember(selectedPane) { selectedPane.presenterFactory(navigator) }
        val configurationState = presenter.present()

        val scope = rememberCoroutineScope()

        return AgentConfigurationScreen.State(
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
                is AgentConfigurationScreen.Event.SelectConfigurationClass -> {
                    updateSelectedClass(event.configurationClass)
                }

                is AgentConfigurationScreen.Event.CreateAgent -> {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AgentConfigurationScreen(
    state: AgentConfigurationScreen.State,
    modifier: Modifier = Modifier,
) {
    val configurationState = state.configurationState
    val isValidConfiguration = configurationState.isValidConfiguration

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp),
                        text = i18n.conversations.newConversationTitle,
                    )
                },
                navigationIcon = {
                    LocalCenterStageControl.current.ContentNavigationButton {
                        Icon(
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                            painter = painterResource(Res.drawable.ic_chat_bubble_outline),
                            contentDescription = i18n.conversations.newConversationIconAria,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            state.eventSink(AgentConfigurationScreen.Event.CreateAgent)
                        },
                        enabled = isValidConfiguration,
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_check),
                            contentDescription = i18n.conversations.createAria,
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 800.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column {
                    AgentConfigurationClassSelector(
                        selectedConfigurationClass = state.selectedAgentClass,
                        onSelectConfigurationClass = {
                            state.eventSink(
                                AgentConfigurationScreen.Event.SelectConfigurationClass(
                                    it
                                )
                            )
                        },
                        agentConfigurationClasses = state.selectableAgentClasses,
                        agentLabels = { state.agentLabels(it) },
                    )

                    key(state.selectedAgentClass) {
                        state.renderer(state.configurationState, Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
