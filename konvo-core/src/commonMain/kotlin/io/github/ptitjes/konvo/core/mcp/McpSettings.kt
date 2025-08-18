package io.github.ptitjes.konvo.core.mcp

import io.github.ptitjes.konvo.core.settings.*
import kotlinx.serialization.*

@Serializable
data class McpSettings(
    val servers: Map<String, ServerSpecification> = emptyMap(),
)

/**
 * Key for MCP settings persisted in the configuration directory.
 */
val McpSettingsKey: SettingsSectionKey<McpSettings> = SettingsSectionKey(
    name = "mcp",
    defaultValue = McpSettings(),
    serializer = McpSettings.serializer(),
)
