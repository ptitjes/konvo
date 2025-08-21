package io.github.ptitjes.konvo.core.roleplay

import io.github.ptitjes.konvo.core.settings.*
import kotlinx.serialization.*

/**
 * Represents a user persona configuration.
 */
@Serializable
data class Persona(
    /** Display name for selectors. */
    val name: String,
    /** Nickname used as the persona name in conversations. */
    val nickname: String,
    /** Optional default lorebook id to attach to this persona. */
    val defaultLorebookId: String? = null,
)

/**
 * Settings for user personas.
 */
@Serializable
data class PersonaSettings(
    val personas: List<Persona> = emptyList(),
)

/**
 * Key for Persona settings persisted in the configuration directory.
 */
val PersonaSettingsKey: SettingsKey<PersonaSettings> = SettingsKey(
    name = "personas",
    defaultValue = PersonaSettings(),
    serializer = PersonaSettings.serializer(),
)
