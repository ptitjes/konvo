package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import com.slack.circuit.retained.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.screen.Screen
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.plugin.roleplay.*
import org.jetbrains.compose.resources.*

data object RoleplayConfigurationView : Screen {
    sealed interface State : AgentConfigurationState<RoleplayAgentConfiguration> {
        data object Loading : State

        data class Available(
            val availableModels: List<ModelCard>,
            val availableCharacters: List<CharacterCard>,
            val availablePersonas: List<Persona>,
            val availableLorebooks: List<Lorebook>,
            val selectedCharacter: CharacterCard?,
            val selectedGreetingIndex: Int?,
            val selectedLorebook: Lorebook?,
            val selectedPersona: Persona?,
            val selectedModel: ModelCard?,
            val eventSink: (Event) -> Unit,
        ) : State

        override val isValidConfiguration: Boolean
            get() {
                if (this !is Available) return false
                return selectedCharacter != null && selectedPersona != null && selectedModel != null
            }

        override fun buildConfiguration(): RoleplayAgentConfiguration {
            check(this is Available)
            check(selectedCharacter != null && selectedPersona != null && selectedModel != null)
            return RoleplayAgentConfiguration(
                characterId = selectedCharacter.id,
                characterGreetingIndex = selectedGreetingIndex,
                personaName = selectedPersona.name,
                modelName = selectedModel.name,
                lorebookId = selectedLorebook?.id,
            )
        }
    }

    sealed interface Event : CircuitUiEvent {
        data class SelectModel(val model: ModelCard) : Event
        data class SelectCharacter(val character: CharacterCard) : Event
        data class SelectGreetingIndex(val index: Int?) : Event
        data class SelectPersona(val persona: Persona) : Event
        data class SelectLorebook(val lorebook: Lorebook?) : Event
        data class GoToSettings(val key: String) : Event
    }
}

internal class RoleplayConfigurationPresenter(
    private val navigator: ConversationNavigator,
    private val modelManager: ModelManager,
    private val characterManager: CharacterManager,
    private val lorebookManager: LorebookManager,
    private val settingsRepository: SettingsRepository,
) : Presenter<RoleplayConfigurationView.State> {
    @Composable
    override fun present(): RoleplayConfigurationView.State {
        val models by modelManager.models.collectAsState(null)
        val characters by characterManager.characters.collectAsState(null)
        val lorebooks by lorebookManager.lorebooks.collectAsState(null)

        return state(models, characters, lorebooks)
    }

    @Composable
    private fun state(
        models: List<ModelCard>?,
        characters: Set<CharacterCard>?,
        lorebooks: List<Lorebook>?,
    ): RoleplayConfigurationView.State {
        return if (models == null || characters == null || lorebooks == null) {
            RoleplayConfigurationView.State.Loading
        } else {
            val roleplaySettings by settingsRepository.getSettings(RoleplaySettingsKey).collectAsState()
            val defaultModelName = roleplaySettings.defaultPreferredModelName
            val defaultPersonaName = roleplaySettings.defaultPersonaName

            val preferredModel = defaultModelName?.let { name ->
                models.firstOrNull { it.name == name }
            }

            val personaSettings by settingsRepository.getSettings(PersonaSettingsKey).collectAsState()
            val preferredPersona = defaultPersonaName?.let { name ->
                personaSettings.personas.firstOrNull { it.name == name }
            } ?: personaSettings.personas.firstOrNull()

            val sortedCharacters = remember(characters) { characters.sortedBy { it.name } }

            var selectedCharacter by rememberRetained { mutableStateOf(sortedCharacters.firstOrNull()) }
            var selectedGreetingIndex by rememberRetained { mutableStateOf<Int?>(null) }
            var selectedLorebook by rememberRetained { mutableStateOf(lorebooks.firstOrNull()) }
            var selectedPersona by rememberRetained { mutableStateOf(preferredPersona) }
            var selectedModel by rememberRetained { mutableStateOf(preferredModel) }

            RoleplayConfigurationView.State.Available(
                availableModels = models,
                availableCharacters = sortedCharacters,
                availableLorebooks = lorebooks,
                availablePersonas = personaSettings.personas,
                selectedCharacter = selectedCharacter,
                selectedGreetingIndex = selectedGreetingIndex,
                selectedLorebook = selectedLorebook,
                selectedPersona = selectedPersona,
                selectedModel = selectedModel,
            ) { event ->
                when (event) {
                    is RoleplayConfigurationView.Event.SelectCharacter -> {
                        if (selectedCharacter?.id != event.character.id) {
                            selectedCharacter = event.character
                            selectedGreetingIndex = null
                        }
                    }

                    is RoleplayConfigurationView.Event.SelectGreetingIndex -> selectedGreetingIndex = event.index
                    is RoleplayConfigurationView.Event.SelectLorebook -> selectedLorebook = event.lorebook
                    is RoleplayConfigurationView.Event.SelectModel -> selectedModel = event.model
                    is RoleplayConfigurationView.Event.SelectPersona -> selectedPersona = event.persona
                    is RoleplayConfigurationView.Event.GoToSettings -> navigator.openSettingsSection(event.key)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoleplayConfigurationPanel(
    state: RoleplayConfigurationView.State,
    modifier: Modifier = Modifier,
) {
    when (val roleplay = state) {
        RoleplayConfigurationView.State.Loading -> FullSizeProgressIndicator(modifier = modifier)
        is RoleplayConfigurationView.State.Available -> Column(modifier = modifier) {
            if (roleplay.availableCharacters.isEmpty()) {
                UnavailabilityPlaceholder(
                    unavailabilityText = i18n.roleplay.rpNoAvailableCharacters,
                    onGoToSettings = {
                        state.eventSink(RoleplayConfigurationView.Event.GoToSettings("characters"))
                    },
                )
            } else {
                val selectedCharacter = roleplay.selectedCharacter ?: roleplay.availableCharacters.first()

                CharacterGridSelector(
                    modifier = Modifier.weight(1f),
                    selectedCharacter = selectedCharacter,
                    onCharacterSelected = { character ->
                        roleplay.eventSink(RoleplayConfigurationView.Event.SelectCharacter(character))
                    },
                    characters = roleplay.availableCharacters,
                )

                if (selectedCharacter.greetings.size > 1) {
                    CharacterGreetingSelector(
                        selectedGreetingIndex = roleplay.selectedGreetingIndex,
                        onGreetingIndexSelected = {
                            roleplay.eventSink(RoleplayConfigurationView.Event.SelectGreetingIndex(it))
                        },
                        character = selectedCharacter,
                        personaName = roleplay.selectedPersona?.nickname ?: "<user>",
                    )
                }
            }

            if (roleplay.availablePersonas.isEmpty()) {
                OutlineBox(
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    label = i18n.roleplay.personaLabel,
                ) {
                    UnavailabilityPlaceholder(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        unavailabilityText = i18n.roleplay.noPersonaDefined,
                        onGoToSettings = {
                            state.eventSink(RoleplayConfigurationView.Event.GoToSettings("personas"))
                        },
                    )
                }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    var showLorebookSheet by remember { mutableStateOf(false) }

                    val personas by rememberSetting(PersonaSettingsKey, emptyList()) { it.personas }

                    Column(modifier = Modifier.weight(1f)) {
                        PersonaSelector(
                            selectedPersona = roleplay.selectedPersona ?: roleplay.availablePersonas.first(),
                            onPersonaSelected = {
                                roleplay.eventSink(RoleplayConfigurationView.Event.SelectPersona(it))
                            },
                            personas = personas,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    FilledTonalIconButton(
                        modifier = Modifier.offset(y = 4.dp),
                        onClick = { showLorebookSheet = true },
                    ) {
                        Icon(
                            painter = painterResource(CoreResources.icons.settings),
                            contentDescription = i18n.roleplay.personaSettingsAria
                        )
                    }

                    if (showLorebookSheet) {
                        ModalBottomSheet(
                            onDismissRequest = { showLorebookSheet = false },
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                            ) {
                                if (roleplay.availableLorebooks.isEmpty()) {
                                    Text(
                                        text = i18n.roleplay.rpNoAvailableLorebooks,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(16.dp),
                                    )
                                } else {
                                    LorebookSelector(
                                        label = i18n.roleplay.additionalLorebookLabel,
                                        selectedLorebook = roleplay.selectedLorebook,
                                        onLorebookSelected = {
                                            roleplay.eventSink(RoleplayConfigurationView.Event.SelectLorebook(it))
                                            showLorebookSheet = false
                                        },
                                        lorebooks = roleplay.availableLorebooks,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            ModelSelector(
                selectedModel = roleplay.selectedModel ?: roleplay.availableModels.firstOrNull(),
                onModelSelected = {
                    roleplay.eventSink(RoleplayConfigurationView.Event.SelectModel(it))
                },
                models = roleplay.availableModels,
                onOpenModelSettings = {
                    state.eventSink(RoleplayConfigurationView.Event.GoToSettings("models"))
                },
            )
        }
    }
}
