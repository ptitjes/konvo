package io.github.ptitjes.konvo.core.ai.mcp

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.sse.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import kotlinx.io.*

internal class McpServerHandler(val specification: ServerSpecification) {
    private var process: Process? = null

    private val httpClient by lazy {
        HttpClient(CIO) {
            install(SSE)
        }
    }

    val client: Client = Client(clientInfo = Implementation(name = "konvo-mcp-client", version = "1.0.0"))

    suspend fun connect() {
        try {
            val processSpecification = specification.process
            val transportSpecification = specification.transport

            if (processSpecification == null && transportSpecification is TransportSpecification.Stdio) {
                error("MCP stdio transport can only be used with spawned processes")
            }

            if (processSpecification != null) {
                println("Starting MCP server with command: ${processSpecification.command.joinToString(" ")}")
                process = ProcessBuilder(processSpecification.command).apply {
                    processSpecification.environment?.let { environment().putAll(it) }
                }.start()
            }

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
            println("Failed to connect to MCP server: $e")
            throw e
        }
    }

    suspend fun close() {
        client.close()
        process?.destroy()
    }
}
