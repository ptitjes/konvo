package io.github.ptitjes.konvo.core.roleplay

import io.github.ptitjes.konvo.core.settings.*
import kotlinx.serialization.*

/**
 * Settings for the Roleplay agent behavior and defaults.
 */
@Serializable
data class RoleplaySettings(
    /** Default persona name used to identify the persona for new roleplay conversations. */
    val defaultPersonaName: String? = null,
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
val RoleplaySettingsKey: SettingsKey<RoleplaySettings> = SettingsKey(
    name = "roleplay",
    defaultValue = RoleplaySettings(),
    serializer = RoleplaySettings.serializer(),
)
