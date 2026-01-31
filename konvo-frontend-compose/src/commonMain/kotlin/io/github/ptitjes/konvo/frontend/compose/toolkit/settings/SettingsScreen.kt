package io.github.ptitjes.konvo.frontend.compose.toolkit.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.frontend.compose.*
import io.github.ptitjes.konvo.frontend.compose.resources.*
import io.github.ptitjes.konvo.frontend.compose.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.translations.*
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
        val localizedTitle = strings.settings.sectionTitles[section.titleKey] ?: section.titleKey

        SettingsScreen(
            title = localizedTitle,
            section = section,
            onBackClick = { navigator.navigateBack() },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    title: String,
    section: SettingsSection,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

                    section.panel(scope)
                }
            }
        }
    }
}
