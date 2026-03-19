package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.resources.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.roleplay.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*
import org.jetbrains.compose.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RoleplayConfigurationPanel(
    state: RoleplayConfigurationView.State,
    onGoToSettingsClick: (titleKey: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val roleplay = state) {
        RoleplayConfigurationView.State.Loading -> FullSizeProgressIndicator(modifier = modifier)
        is RoleplayConfigurationView.State.Available -> Column(modifier = modifier) {
            if (roleplay.availableCharacters.isEmpty()) {
                UnavailabilityPlaceholder(
                    unavailabilityText = i18n.conversations.rpNoAvailableCharacters,
                    onGoToSettings = { onGoToSettingsClick("characters") },
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
                UnavailabilityPlaceholder(
                    unavailabilityText = i18n.conversations.rpNoAvailablePersonas,
                    onGoToSettings = { onGoToSettingsClick("personas") },
                )
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
                            painter = painterResource(Res.drawable.ic_settings),
                            contentDescription = i18n.conversations.personaSettingsAria
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
                                        text = i18n.conversations.rpNoAvailableLorebooks,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.padding(16.dp),
                                    )
                                } else {
                                    LorebookSelector(
                                        label = i18n.conversations.additionalLorebookLabel,
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

            if (roleplay.availableModels.isEmpty()) {
                UnavailabilityPlaceholder(
                    unavailabilityText = i18n.conversations.rpNoAvailableModel,
                    onGoToSettings = { onGoToSettingsClick("models") },
                )
            } else {
                ModelSelector(
                    selectedModel = roleplay.selectedModel ?: roleplay.availableModels.first(),
                    onModelSelected = {
                        roleplay.eventSink(RoleplayConfigurationView.Event.SelectModel(it))
                    },
                    models = roleplay.availableModels
                )
            }
        }
    }
}