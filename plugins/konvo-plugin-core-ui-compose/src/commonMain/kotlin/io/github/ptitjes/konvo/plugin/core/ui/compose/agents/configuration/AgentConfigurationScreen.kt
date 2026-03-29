package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.slack.circuit.foundation.*
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import org.jetbrains.compose.resources.*
import org.kodein.di.*
import org.kodein.di.compose.*

@Composable
fun AgentConfigurationScreen(
    navigator: ConversationNavigator,
    modifier: Modifier = Modifier.Companion,
) {
    val di = localDI()
    val circuit = remember { buildCircuit(di, navigator) }
    CircuitCompositionLocals(circuit) {
        CircuitContent(AgentConfigurationView, modifier = modifier.fillMaxSize())
    }
}

private fun buildCircuit(di: DI, navigator: ConversationNavigator): Circuit {
    val agentConfigurationPresenter by di.newInstance {
        new(::AgentConfigurationPresenter, navigator)
    }

    return Circuit.Builder()
        .addPresenterFactory { screen, _, _ ->
            when (screen) {
                is AgentConfigurationView -> agentConfigurationPresenter
                else -> null
            }
        }
        .addUi<AgentConfigurationView, AgentConfigurationView.State> { state, modifier ->
            NewConversationScreenLayout(
                state = state,
                onProviderSettingsClick = { navigator.openSettingsSection("models") },
                onGoToSettingsClick = { navigator.openSettingsSection(it) },
                modifier = modifier,
            )
        }
        .build()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewConversationScreenLayout(
    state: AgentConfigurationView.State,
    onProviderSettingsClick: () -> Unit,
    onGoToSettingsClick: (titleKey: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val configurationState = state.configurationState
    val isValidConfiguration = configurationState.isValidConfiguration

    val snackBarHostState = remember { SnackbarHostState() }

    val modelManager by rememberInstance<ModelManager>()

    LaunchedEffect(Unit) {
        modelManager.providersInError.collect { providers ->
            if (providers != null) {
                val providerNames = providers.joinToString(", ")
                val result = snackBarHostState.showSnackbar(
                    message = "Failed to load models from $providerNames",
                    actionLabel = "Settings",
                    withDismissAction = true,
                    duration = SnackbarDuration.Indefinite,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    onProviderSettingsClick()
                }
            }
        }
    }

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
                    LocalCenterStageControl.current.NavigationButton {
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
                            state.eventSink(AgentConfigurationView.Event.CreateAgent)
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
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
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
                AgentConfigurationPanel(
                    state = state,
                    onGoToSettingsClick = onGoToSettingsClick,
                )
            }
        }
    }
}
