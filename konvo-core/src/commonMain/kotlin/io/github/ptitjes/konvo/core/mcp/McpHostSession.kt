package io.github.ptitjes.konvo.core.mcp

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.tools.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.*
import kotlin.coroutines.*

class McpHostSession(
    coroutineContext: CoroutineContext,
    serverSettingsManager: McpServerSpecificationsManager,
) : AutoCloseable {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val sessionJob = SupervisorJob(coroutineContext[Job])
    private val coroutineScope = CoroutineScope(coroutineContext + Dispatchers.IO + sessionJob)

    private lateinit var serverSpecifications: StateFlow<Map<String, ServerSpecification>>

    private val mutex = Mutex()
    private val selectedServerNames = atomic(setOf<String>())
    private val servers = MutableStateFlow(mapOf<String, Server>())

    private class Server(
        val specification: ServerSpecification,
        val handler: McpServerHandler,
    )

    private val initialized = CompletableDeferred<Unit>()
    private suspend fun awaitInitialized() = initialized.await()

    init {
        coroutineScope.launch {
            serverSpecifications = serverSettingsManager.specifications.stateIn(coroutineScope)
            initialized.complete(Unit)

            launch {
                serverSpecifications.collect { serverSpecifications ->
                    updateServers(serverSpecifications)
                }
            }
        }
    }

    private suspend fun updateServers(currentServerSpecifications: Map<String, ServerSpecification>): Unit =
        mutex.withLock {
            val currentServerNames = currentServerSpecifications.keys

            val serversToAdd = currentServerSpecifications.filter { (serverName, _) ->
                serverName !in servers.value && serverName in selectedServerNames.value
            }
            val serversToRemove = servers.value - currentServerNames
            val serversToRestart = servers.value.filter { (serverName, server) ->
                serverName in currentServerSpecifications && currentServerSpecifications[serverName] != server.specification
            }

            if (serversToAdd.isNotEmpty()) {
                val newServers = serversToAdd.mapValues { (serverName, newSpecification) ->
                    Server(
                        specification = newSpecification,
                        handler = McpServerHandler(coroutineScope.coroutineContext, serverName, newSpecification),
                    )
                }

                logger.info { "Adding MCP servers: ${serversToAdd.keys}" }
                logger.info { "Starting MCP servers: ${serversToAdd.keys}" }
                newServers.values.forEach { it.handler.startAndConnect() }
                servers.value += newServers
            }

            if (serversToRemove.isNotEmpty()) {
                logger.info { "Removing MCP servers: ${serversToRemove.keys}" }
                servers.value -= serversToRemove.keys
                logger.info { "Stopping MCP servers: ${serversToRemove.keys}" }
                serversToRemove.values.forEach { it.handler.disconnectAndStop() }
            }

            if (serversToRestart.isNotEmpty()) {
                val replacementServers = serversToRestart.mapValues { (serverName, _) ->
                    val newSpecification = currentServerSpecifications[serverName]!!
                    Server(
                        specification = newSpecification,
                        handler = McpServerHandler(coroutineScope.coroutineContext, serverName, newSpecification),
                    )
                }

                logger.info { "Restarting MCP servers: ${serversToRestart.keys}" }
                logger.info { "Starting replacement MCP servers: ${serversToRestart.keys}" }
                replacementServers.values.forEach { it.handler.startAndConnect() }
                servers.value += replacementServers
                logger.info { "Stopping obsolete MCP servers: ${serversToRestart.keys}" }
                serversToRestart.values.forEach { it.handler.disconnectAndStop() }
            }
        }

    suspend fun addServers(serverNames: Set<String>) {
        selectedServerNames.update { it + serverNames }
        serverNames.forEach { serverName -> maybeStartServer(serverName) }
    }

    suspend fun addServer(serverName: String) {
        selectedServerNames.update { it + serverName }
        maybeStartServer(serverName)
    }

    suspend fun removeServer(serverName: String) {
        selectedServerNames.update { it - serverName }
        maybeStopServer(serverName)
    }

    private suspend fun maybeStartServer(serverName: String) {
        awaitInitialized()
        mutex.withLock {
            if (serverName in servers.value) return@withLock
            val specification =
                serverSpecifications.value[serverName] ?: error("Server specification '$serverName' not found")

            val server = Server(
                specification = specification,
                handler = McpServerHandler(coroutineScope.coroutineContext, serverName, specification),
            )
            logger.info { "Starting new MCP server: $serverName" }
            server.handler.startAndConnect()
            logger.info { "Adding new MCP server: $serverName" }
            servers.value += serverName to server
        }
    }

    private suspend fun maybeStopServer(serverName: String) {
        awaitInitialized()
        mutex.withLock {
            if (serverName !in servers.value) return@withLock

            val server = servers.value[serverName]
            logger.info { "Removing MCP server: $serverName" }
            servers.value -= serverName
            logger.info { "Stopping new MCP server: $serverName" }
            server?.handler?.disconnectAndStop()
        }
    }

    override fun close() {
        sessionJob.cancel()
    }

    val tools: Flow<List<ToolCard>>
        get() =
            servers.map { servers ->
                servers.values
                    .map { it.handler.name to it.handler.client }
                    .flatMap { (clientName, client) ->
                        val serverCapabilities = client.serverCapabilities
                        if (serverCapabilities == null || serverCapabilities.tools == null) return@flatMap emptyList()

                        client.listTools()?.tools?.map { tool ->
                            McpToolCard(
                                clientName = clientName,
                                client = client,
                                tool = tool,
                                permissions = ToolPermissions(default = ToolPermission.ALLOW),
                            )
                        } ?: emptyList()
                    }
            }
}
