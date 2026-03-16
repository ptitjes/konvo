package io.github.ptitjes.konvo.plugin.core.mcp

import ai.koog.agents.core.tools.*
import ai.koog.agents.core.tools.Tool
import io.modelcontextprotocol.kotlin.sdk.client.*
import io.modelcontextprotocol.kotlin.sdk.types.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.json.*

/**
 * A Tool implementation that calls an MCP (Model Context Protocol) tool.
 *
 * This class serves as a bridge between the agent framework's Tool interface and the MCP SDK.
 * It allows MCP tools to be used within the agent framework by:
 * 1. Converting agent framework tool arguments to MCP tool arguments
 * 2. Calling the MCP tool through the MCP client
 * 3. Converting MCP tool results back to agent framework tool results
 */
public class StructuredMcpTool(
    private val mcpClient: Client,
    descriptor: ToolDescriptor,
) : Tool<JsonObject, CallToolResult?>(
    argsSerializer = JsonObject.serializer(),
    resultSerializer = CallToolResult.serializer().nullable,
    descriptor = descriptor
) {

    /**
     * Executes the MCP tool with the given arguments.
     *
     * This method calls the MCP tool through the MCP client and converts the result
     * to a Result object that can be used by the agent framework.
     *
     * @param args The arguments for the MCP tool call.
     * @return The result of the MCP tool call.
     */
    override suspend fun execute(args: JsonObject): CallToolResult {
        return mcpClient.callTool(
            name = descriptor.name,
            arguments = args
        )
    }

    /**
     * Postprocess result string representation for LLMs a bit, removing unnecessary meta fields.
     */
    override fun encodeResultToString(result: CallToolResult?): String {
        val jsonContent = result?.structuredContent ?: JsonNull
        return json.encodeToString(jsonContent)
    }
}
