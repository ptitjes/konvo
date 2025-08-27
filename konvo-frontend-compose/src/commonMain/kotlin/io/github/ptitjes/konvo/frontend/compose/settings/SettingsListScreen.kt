package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.frontend.compose.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.viewmodels.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

@Composable
fun SettingsListScreen(
    navigator: Navigator,
    viewModel: SettingsListViewModel = viewModel(),
) {
    val sections by viewModel.sections.collectAsState()

    val paneType = LocalListDetailPaneType.current
    LaunchedEffect(paneType) {
        if (paneType == ListDetailPaneType.TwoPane && navigator.backStack.last() == Destination.Setting.List) {
            navigator.navigateToSettingSection(sections.first().titleKey)
        }
    }

    val selectedSection = navigator.selectedSettingSectionKey?.let { key -> sections.findSectionByTitleKey(key) }

    SettingsListScreen(
        sections = sections,
        selectedSection = selectedSection,
        onSelectSection = { navigator.navigateToSettingSection(it.titleKey) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsListScreen(
    sections: List<SettingsSection>,
    selectedSection: SettingsSection?,
    onSelectSection: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier.Companion,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.settings.listTitle,
                    )
                },
            )
        },
    ) { paddingValues ->
        val flattenedSections = remember(sections) { sections.flatten() }

        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(flattenedSections, key = { it.section.titleKey }) { flattenedSection ->
                val section = flattenedSection.section
                val selected = section == selectedSection
                val selectSectionAria = strings.settings.selectSectionAria
                val localizedTitle = strings.settings.sectionTitles[section.titleKey] ?: section.titleKey

                Surface(
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                onClick(label = selectSectionAria, action = null)
                            }
                            .clickable(role = Role.Button, onClick = { onSelectSection(section) })
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 16.dp * flattenedSection.depth),
                            text = localizedTitle,
                        )
                    }
                }
            }
        }
    }
}

fun List<SettingsSection>.flatten(): List<FlattenSettingsSection> {
    fun List<SettingsSection>.flatten(depth: Int): List<FlattenSettingsSection> {
        return flatMap { section ->
            listOf(FlattenSettingsSection(section, depth)) + section.children.flatten(depth + 1)
        }
    }

    return flatten(0)
}

data class FlattenSettingsSection(
    val section: SettingsSection,
    val depth: Int,
)
