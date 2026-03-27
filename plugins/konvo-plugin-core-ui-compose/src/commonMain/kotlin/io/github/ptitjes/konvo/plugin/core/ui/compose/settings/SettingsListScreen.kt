package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import kotlinx.serialization.*
import org.jetbrains.compose.resources.*

@Serializable
data object SettingsListScreen : SettingsScreen {
    override fun toString(): String = "settings"

    internal sealed interface State : CircuitUiState {
        data object Loading : State
        data class Loaded(
            val sections: List<FlattenSettingsSection>,
            val selectedSection: SettingsSection<*>? = null,
            val eventSink: (Event) -> Unit,
        ) : State
    }

    internal sealed interface Event : CircuitUiEvent {
        data class NavigateTo(val key: String) : Event
    }
}

internal data class FlattenSettingsSection(
    val section: SettingsSection<*>,
    val depth: Int,
)

internal class SettingsListPresenter(
    private val navigator: SettingsNavigator,
    private val sectionManager: SettingsSectionManager,
) : Presenter<SettingsListScreen.State> {
    @Composable
    override fun present(): SettingsListScreen.State {
        val sections = remember { sectionManager.sections.toList() }
        val flattenedSections = remember(sections) {
            sections.recursivelySortedBy { it.titleKey }.flatten()
        }

        return SettingsListScreen.State.Loaded(flattenedSections) { event ->
            when (event) {
                is SettingsListScreen.Event.NavigateTo -> {
                    navigator.navigateToSettingSection(event.key)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SettingsListScreen(
    state: SettingsListScreen.State,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is SettingsListScreen.State.Loading -> FullSizeProgressIndicator(modifier)
        is SettingsListScreen.State.Loaded -> SettingsListScreen(
            modifier = modifier,
            flattenedSections = state.sections,
            selectedSection = state.selectedSection,
            onSelectSection = { state.eventSink(SettingsListScreen.Event.NavigateTo(it.titleKey)) }
        )
    }
}

@Composable
private fun SettingsListScreen(
    modifier: Modifier = Modifier,
    flattenedSections: List<FlattenSettingsSection>,
    selectedSection: SettingsSection<*>?,
    onSelectSection: (SettingsSection<*>) -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    val contentColor = MaterialTheme.colorScheme.onSurface

    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = contentColor,
        topBar = {
            // Reserved for the settings search bar
            @OptIn(ExperimentalMaterial3Api::class)
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = containerColor,
                    titleContentColor = contentColor,
                ),
                title = { },
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(flattenedSections, key = { it.section.titleKey }) { flattenedSection ->
                val section = flattenedSection.section
                val selected = section == selectedSection
                val selectSectionAria = i18n.settings.selectSectionAria
                val title = section.title()
                val depthPadding = 16.dp * flattenedSection.depth

                Surface(
                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                    shape = MaterialTheme.shapes.extraSmall,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics {
                                onClick(label = selectSectionAria, action = null)
                            }
                            .clickable(role = Role.Button, onClick = { onSelectSection(section) })
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(depthPadding))
                        Icon(
                            modifier = Modifier.size(20.dp),
                            painter = painterResource(section.icon),
                            contentDescription = title,
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = title)
                    }
                }
            }
        }
    }
}

private fun <R : Comparable<R>> List<SettingsSection<*>>.recursivelySortedBy(selector: (SettingsSection<*>) -> R?): List<SettingsSection<*>> {
    fun List<SettingsSection<*>>.recursivelySorted(): List<SettingsSection<*>> {
        return sortedBy { selector(it) }
            .map { section -> section.copy(children = section.children.recursivelySorted()) }
    }

    return recursivelySorted()
}

private fun List<SettingsSection<*>>.flatten(): List<FlattenSettingsSection> {
    fun List<SettingsSection<*>>.flatten(depth: Int): List<FlattenSettingsSection> {
        return flatMap { section ->
            listOf(FlattenSettingsSection(section, depth)) + section.children.flatten(depth + 1)
        }
    }

    return flatten(0)
}
