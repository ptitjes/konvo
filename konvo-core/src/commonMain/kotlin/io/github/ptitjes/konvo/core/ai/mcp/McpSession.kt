package io.github.ptitjes.konvo.core.ai.mcp

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.*
import kotlin.coroutines.*

class McpSession(
    coroutineContext: CoroutineContext,
    settingsRepository: SettingsRepository,
) : AutoCloseable {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val sessionJob = SupervisorJob()
    private val coroutineScope = CoroutineScope(coroutineContext + Dispatchers.IO + sessionJob)

    private lateinit var settings: StateFlow<McpSettings>

    private val mutex = Mutex()
    private val servers = mutableMapOf<String, Server>()

    private class Server(
        val specification: ServerSpecification,
        val handler: McpServerHandler,
    )

    init {
        coroutineScope.launch {
            settings = settingsRepository.getSettings(McpSettingsKey).stateIn(this)

            launch { observeSettingsChanges() }
        }
    }

    private suspend fun observeSettingsChanges() {
        settings.collect { settings ->
            updateServers(settings.servers)
        }
    }

    private suspend fun updateServers(currentServers: Map<String, ServerSpecification>): Unit = mutex.withLock {
        val currentServerNames = currentServers.keys

        val serversToRemove = servers - currentServerNames
        val serversToRestart = servers.filter { (serverName, server) ->
            serverName in currentServers && currentServers[serverName] != server.specification
        }

        logger.info { "Removing MCP servers: ${serversToRemove.keys}" }
        servers -= serversToRemove.keys
        logger.info { "Stopping MCP servers: ${serversToRemove.keys}" }
        serversToRemove.values.forEach { it.handler.disconnectAndStop() }

        val replacementServers = serversToRestart.mapValues { (serverName, _) ->
            val newSpecification = currentServers[serverName]!!
            Server(
                specification = newSpecification,
                handler = McpServerHandler(coroutineScope.coroutineContext, serverName, newSpecification),
            )
        }

        logger.info { "Restarting MCP servers: ${serversToRestart.keys}" }
        logger.info { "Starting replacement MCP servers: ${serversToRestart.keys}" }
        replacementServers.values.forEach { it.handler.startAndConnect() }
        servers += replacementServers
        logger.info { "Stopping obsolete MCP servers: ${serversToRestart.keys}" }
        serversToRestart.values.forEach { it.handler.disconnectAndStop() }
    }

    suspend fun addServer(serverName: String) = mutex.withLock {
        if (serverName in servers) return@withLock
        val specification = settings.value.servers[serverName] ?: error("Server specification '$serverName' not found")

        logger.info { "Adding new MCP server: $serverName" }
        val server = Server(
            specification = specification,
            handler = McpServerHandler(coroutineScope.coroutineContext, serverName, specification),
        )
        server.handler.startAndConnect()
        servers[serverName] = server
    }

    suspend fun removeServer(serverName: String) = mutex.withLock {
        if (serverName !in servers) return@withLock

        logger.info { "Removing MCP server: $serverName" }
        servers[serverName]?.handler?.disconnectAndStop()
        servers -= serverName
    }

    override fun close() {
        sessionJob.cancel()
    }
}
