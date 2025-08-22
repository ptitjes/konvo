package io.github.ptitjes.konvo.frontend.compose.mcp

/**
 * Translated strings for the MCP package (servers selector and settings UI).
 */
internal data class McpStrings(
    val configuredServersTitle: String,
    val configuredServersDescription: String,
    val addServerAria: String,
    val noServersMessage: String,
    val editServerAria: String,
    val deleteServerAria: String,
    val deleteServerDialogTitle: String,
    val deleteServerDialogText: (String) -> String,
    val deleteConfirm: String,
    val cancel: String,
    val nameLabel: String,
    val transportLabel: String,
    val removeServerAria: String,
    val sseUrlLabel: String,
    val reconnectionTimeLabel: String,
    val runAsProcessLabel: String,
    val commandLabel: String,
    val environmentLabel: String,
    val selectorLabel: String,
    val selectorEmpty: String,
)
