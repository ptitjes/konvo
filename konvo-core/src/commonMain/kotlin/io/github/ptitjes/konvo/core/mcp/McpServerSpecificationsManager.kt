package io.github.ptitjes.konvo.core.mcp

import kotlinx.coroutines.flow.Flow

/**
 * Dedicated manager for MCP server specifications.
 */
interface McpServerSpecificationsManager {
    /** A flow of the available MCP server specifications keyed by server name. */
    val specifications: Flow<Map<String, ServerSpecification>>
}
