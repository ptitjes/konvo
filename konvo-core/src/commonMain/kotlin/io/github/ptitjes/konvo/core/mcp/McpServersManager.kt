package io.github.ptitjes.konvo.core.mcp

import io.github.oshai.kotlinlogging.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

class McpServersManager(
    coroutineContext: CoroutineContext,
    private val specifications: Map<String, ServerSpecification>?,
) : CoroutineScope {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val job = SupervisorJob(coroutineContext[Job])
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception caught" }
    }

    override val coroutineContext: CoroutineContext =
        coroutineContext + Dispatchers.Default + job + handler

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

    val clients: Map<String, Client>
        get() =
            handlers?.mapValues { (_, handler) -> handler.client } ?: emptyMap()
}
