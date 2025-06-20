package io.github.ptitjes.konvo.core.ai.mcp

import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlin.coroutines.*

class McpServersManager(
    private val coroutineContext: CoroutineContext,
    private val specifications: Map<String, ServerSpecification>?,
) {
    private var handlers: Map<String, McpServerHandler>? = null

    suspend fun startAndConnectServers() {
        handlers = specifications?.mapValues { (name, specification) ->
            McpServerHandler(coroutineContext, name, specification).also { it.startAndConnect() }
        }
    }

    suspend fun disconnectAndStopServers() {
        handlers?.forEach { (_, handler) ->
            handler.disconnectAndStop()
        }
    }

    val clients: Map<String, Client> get() =
        handlers?.mapValues { (_, handler) -> handler.client } ?: emptyMap()
}
