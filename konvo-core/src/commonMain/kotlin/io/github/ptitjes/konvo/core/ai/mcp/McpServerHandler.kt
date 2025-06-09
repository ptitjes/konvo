package io.github.ptitjes.konvo.core.ai.mcp

import io.github.oshai.kotlinlogging.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.sse.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.*

internal class McpServerHandler(
    private val name: String,
    private val specification: ServerSpecification,
) {

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var process: Process? = null

    private val httpClient by lazy {
        HttpClient(CIO) {
            install(SSE)
        }
    }

    val client: Client = Client(clientInfo = Implementation(name = "konvo-mcp-client", version = "1.0.0"))

    suspend fun startAndConnect() = withContext(Dispatchers.IO) {
        val processSpecification = specification.process
        val transportSpecification = specification.transport

        if (processSpecification == null && transportSpecification is TransportSpecification.Stdio) {
            logger.error { "Invalid MCP server '$name': stdio transport can only be used with spawned processes" }
            return@withContext
        }

        try {
            if (processSpecification != null) {
                logger.info {
                    val commandString = processSpecification.command.joinToString(" ")
                    "Starting MCP server '$name' with command: $commandString"
                }

                process = ProcessBuilder(processSpecification.command).apply {
                    processSpecification.environment?.let { environment().putAll(it) }
                }.start()
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to start process for MCP server '$name'" }
            return@withContext
        }

        try {
            val transport = when (transportSpecification) {
                is TransportSpecification.Stdio -> StdioClientTransport(
                    input = process!!.inputStream.asSource().buffered(),
                    output = process!!.outputStream.asSink().buffered()
                )

                is TransportSpecification.Sse -> SseClientTransport(
                    client = httpClient,
                    urlString = transportSpecification.urlString,
                    reconnectionTime = transportSpecification.reconnectionTime,
                    requestBuilder = transportSpecification.requestBuilder,
                )
            }

            client.connect(transport)
        } catch (e: Exception) {
            logger.error(e) { "Failed to connect to MCP server '$name'" }
        }
    }

    suspend fun disconnectAndStop() = withContext(Dispatchers.IO) {
        client.close()
        process?.destroy()
    }
}
