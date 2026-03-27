package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.viewmodels.*
import org.jetbrains.compose.resources.*

@Composable
fun SettingsScreen(
    titleKey: String,
    navigator: SettingsNavigator,
    viewModel: SettingsListViewModel = viewModel(),
) {
    key(titleKey) {
        val sections by viewModel.sections.collectAsState()
        val section = remember { sections.findSectionByTitleKey(titleKey)!! }

        SettingsScreen(section, navigator)
    }
}

@Composable
private fun <S : SettingsSectionState> SettingsScreen(
    section: SettingsSection<S>,
    navigator: SettingsNavigator,
) {
    val presenter = remember { section.presenterFactory() }

    val state = presenter.present()

    SettingsScreen(
        section = section,
        state = state,
        onBackClick = { navigator.navigateBack() },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <S : SettingsSectionState> SettingsScreen(
    section: SettingsSection<S>,
    state: S,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = section.title()

    val paneType = LocalListDetailPaneType.current
    val snackbarHostState = remember { SnackbarHostState() }

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
                        IconButton(onClick = onBackClick) {
                            Icon(
                                painter = painterResource(Res.drawable.ic_arrow_back),
                                contentDescription = "Back"
                            )
                        }
                    } else {
                        Icon(
                            modifier = Modifier.padding(start = 16.dp, end = 8.dp),
                            painter = painterResource(section.icon),
                            contentDescription = title,
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 800.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val baseModifier = if (section.scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
                Column(modifier = baseModifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
                    val scope = remember {
                        object : SettingsPanelScope {
                            override suspend fun showSnackbar(
                                message: String,
                                actionLabel: String?,
                                withDismissAction: Boolean,
                                duration: SnackbarDuration,
                            ): SnackbarResult =
                                snackbarHostState.showSnackbar(message, actionLabel, withDismissAction, duration)
                        }
                    }

                    section.panel(scope, state)
                }
            }
        }
    }
}
