package io.github.ptitjes.konvo.core.characters

import io.github.ptitjes.konvo.core.settings.*
import kotlinx.serialization.*

@Serializable
data class CharacterSettings(
    val filteredTags: List<String> = emptyList(),
)

val CharacterSettingsKey: SettingsSectionKey<CharacterSettings> = SettingsSectionKey(
    name = "characters",
    defaultValue = CharacterSettings(),
    serializer = CharacterSettings.serializer(),
)
