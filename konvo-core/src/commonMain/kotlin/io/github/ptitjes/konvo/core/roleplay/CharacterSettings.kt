package io.github.ptitjes.konvo.core.roleplay

import io.github.ptitjes.konvo.core.settings.*
import kotlinx.serialization.*

@Serializable
data class CharacterSettings(
    val filteredTags: List<String> = emptyList(),
)

val CharacterSettingsKey: SettingsKey<CharacterSettings> = SettingsKey(
    name = "characters",
    defaultValue = CharacterSettings(),
    serializer = CharacterSettings.serializer(),
)
