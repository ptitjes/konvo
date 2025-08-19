package io.github.ptitjes.konvo.core.agents

import io.github.ptitjes.konvo.core.settings.*
import kotlinx.serialization.*

/**
 * Settings for the Roleplay agent behavior and defaults.
 */
@Serializable
data class RoleplayAgentSettings(
    /** Default name to use for the user persona in roleplay conversations. */
    val defaultUserPersonaName: String = "User",
    /** Default preferred model name to use for roleplay conversations. */
    val defaultPreferredModelName: String? = null,
    /** Optional default system prompt to use when the character card does not define one. */
    val defaultSystemPrompt: String = "",
)

/**
 * Key for Roleplay Agent settings persisted in the configuration directory.
 */
val RoleplayAgentSettingsKey: SettingsSectionKey<RoleplayAgentSettings> = SettingsSectionKey(
    name = "roleplay-agent",
    defaultValue = RoleplayAgentSettings(),
    serializer = RoleplayAgentSettings.serializer(),
)
