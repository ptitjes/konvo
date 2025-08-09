package io.github.ptitjes.konvo.core.ai.mcp

import io.github.oshai.kotlinlogging.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.sse.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlin.coroutines.*
import kotlin.time.Duration.Companion.seconds

internal class McpServerHandler(
    coroutineContext: CoroutineContext,
    private val name: String,
    private val specification: ServerSpecification,
) {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val coroutineScope = CoroutineScope(coroutineContext + Dispatchers.IO + SupervisorJob())

    private var process: Process? = null
    private var processObserverJob: Job? = null

    private val httpClient by lazy {
        HttpClient(CIO) {
            install(SSE)
        }
    }

    val client: Client = Client(clientInfo = Implementation(name = "konvo-mcp-client", version = "1.0.0"))

    suspend fun startAndConnect() {
        val processSpecification = specification.process
        val transportSpecification = specification.transport

        if (processSpecification == null && transportSpecification is TransportSpecification.Stdio) {
            logger.error { "Invalid MCP server '$name': stdio transport can only be used with spawned processes" }
            return
        }

        try {
            if (processSpecification != null) {
                logger.info {
                    val commandString = processSpecification.command.joinToString(" ")
                    "Starting MCP server '${name}' with command: $commandString"
                }

                process = spawnProcess(processSpecification.command, processSpecification.environment)
                processObserverJob = launchProcessObserver()

                logger.info { "Started MCP server '${name}'" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to start process for MCP server '$name'" }
            return
        }

        try {
            val transport = when (transportSpecification) {
                is TransportSpecification.Stdio -> StdioClientTransport(
                    input = process!!.inputStream.asSource().buffered(),
                    output = process!!.outputStream.asSink().buffered()
                )

                is TransportSpecification.Sse -> SseClientTransport(
                    client = httpClient,
                    urlString = transportSpecification.url,
                    reconnectionTime = transportSpecification.reconnectionTime,
                ) {
                    println(this.url)
                }
            }

            client.connect(transport)

            logger.info { "Connected to MCP server '${name}'" }
        } catch (e: Exception) {
            logger.error(e) { "Failed to connect to MCP server '$name'" }
        }
    }

    private fun launchProcessObserver(): Job = coroutineScope.launch {
        val process = process!!

        launch {
            while (isActive && process.isAlive) delay(1.seconds)

            val exitCode = process.exitValue()
            if (exitCode != 0) {
                logger.error { "MCP server '${name}' exited with code: $exitCode" }
            }
        }

        launch {
            process.errorStream.asSource().buffered().use { errorStream ->
                while (isActive && process.isAlive) {
                    val errorLine = errorStream.readLine() ?: break
                    logger.error { "MCP server '${name}' error: $errorLine" }
                }
            }
        }
    }

    suspend fun disconnectAndStop() = withContext(Dispatchers.IO) {
        client.close()
        processObserverJob?.cancel()
        process?.destroy()
    }
}

private suspend fun spawnProcess(command: List<String>, environment: Map<String, String>?): Process? =
    withContext(Dispatchers.IO) {
        ProcessBuilder(command).apply { environment?.let { environment().putAll(it) } }.start()
    }
