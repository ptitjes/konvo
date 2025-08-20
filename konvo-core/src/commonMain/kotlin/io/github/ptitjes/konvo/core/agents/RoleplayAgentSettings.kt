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
    val defaultSystemPrompt: String = DEFAULT_SYSTEM_PROMPT,
    /** Optional default scan depth to use when the character card does not define one. */
    val defaultScanDepth: Int = DEFAULT_SCAN_DEPTH,
    /** Optional default token budget to use when the character card does not define one. */
    val defaultTokenBudget: Int = DEFAULT_TOKEN_BUDGET,
    /** Optional default recursive scanning flag to use when the character card does not define one. */
    val defaultRecursiveScanning: Boolean = DEFAULT_RECURSIVE_SCANNING,
)

internal const val DEFAULT_SYSTEM_PROMPT =
    "Write {{char}}'s next reply in a fictional chat between {{char}} and {{user}}."
internal const val DEFAULT_SCAN_DEPTH = 2
internal const val DEFAULT_TOKEN_BUDGET = 512
internal const val DEFAULT_RECURSIVE_SCANNING = false

/**
 * Key for Roleplay Agent settings persisted in the configuration directory.
 */
val RoleplayAgentSettingsKey: SettingsSectionKey<RoleplayAgentSettings> = SettingsSectionKey(
    name = "roleplay-agent",
    defaultValue = RoleplayAgentSettings(),
    serializer = RoleplayAgentSettings.serializer(),
)
