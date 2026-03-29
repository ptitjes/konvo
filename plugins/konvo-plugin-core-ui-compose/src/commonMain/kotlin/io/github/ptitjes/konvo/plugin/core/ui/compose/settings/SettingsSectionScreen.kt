package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.slack.circuit.foundation.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import kotlinx.serialization.*
import org.jetbrains.compose.resources.*

@Serializable
data class SettingsSectionScreen(val key: String) : SettingsScreen {
    override fun toString(): String = "settings/$key"

    internal data class State(
        val key: String,
        val icon: DrawableResource,
        val title: String,
        val scrollable: Boolean,
        val eventSink: (Event) -> Unit,
    ) : SettingsSectionState

    internal sealed interface Event : CircuitUiEvent {
        data object NavigateBack : Event
    }
}

internal class SettingsSectionPresenter(
    private val screen: SettingsSectionScreen,
    private val navigator: SettingsNavigator,
    private val sectionManager: SettingsSectionManager,
) : Presenter<SettingsSectionScreen.State> {
    @Composable
    override fun present(): SettingsSectionScreen.State {
        val section = remember(screen.key) { sectionManager.sectionForKey(screen.key) }

        return SettingsSectionScreen.State(
            key = screen.key,
            icon = section.icon,
            title = section.title(),
            scrollable = section.scrollable,
        ) { event ->
            when (event) {
                SettingsSectionScreen.Event.NavigateBack -> {
                    navigator.goBack()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsSectionScreen(
    state: SettingsSectionScreen.State,
    modifier: Modifier = Modifier,
) {
    val title = state.title

    val paneType = LocalListDetailPaneType.current

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        modifier =
                            if (paneType != ListDetailPaneType.OnePane) Modifier.padding(start = 16.dp)
                            else Modifier,
                        text = title,
                    )
                },
                navigationIcon = {
                    if (paneType == ListDetailPaneType.OnePane) {
                        IconButton(
                            onClick = { state.eventSink(SettingsSectionScreen.Event.NavigateBack) },
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    } else {
                        Icon(
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                            painter = painterResource(state.icon),
                            contentDescription = title,
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 800.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val baseModifier = if (state.scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
                Column(modifier = baseModifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                    CircuitContent(SettingsSectionView(state.key))
                }
            }
        }
    }
}
