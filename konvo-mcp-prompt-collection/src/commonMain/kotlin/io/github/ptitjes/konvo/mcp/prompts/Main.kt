package io.github.ptitjes.konvo.mcp.prompts

import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.*
import kotlinx.cli.*
import kotlinx.coroutines.*
import kotlinx.io.*

/**
 * Start the Konvo prompt collection MCP server.
 *
 * - "--stdio": Runs the MCP server using standard input/output.
 * - "--sse -p <port>": Runs the MCP server using SSE.
 */
fun main(args: Array<String>) {
    val parser = ArgParser(
        "konvo-tool-web",
        useDefaultHelpShortName = true,
    )

    val stdio by parser.option(
        type = ArgType.Boolean,
        description = "Use STDIO transport",
    )
    val sse by parser.option(
        type = ArgType.Boolean,
        description = "Use SSE transport",
    )
    val port by parser.option(
        type = ArgType.Int,
        shortName = "p",
        description = "The SSE port to use, (defaults to 3001)",
    ).default(3001)

    parser.parse(args)

    if (stdio == true && sse == true) System.err.println("Choose either STDIO or SSE transport")

    when {
        stdio == true -> runMcpServerUsingStdio()
        sse == true -> runSseMcpServerUsingKtorPlugin(port)
        else -> {
            System.err.println("Choose either STDIO or SSE transport")
        }
    }
}

fun configureServer(): Server {
    val server = Server(
        Implementation(
            name = "konvo-prompt-collection",
            version = "1.0.0"
        ),
        ServerOptions(
            capabilities = ServerCapabilities(
                prompts = ServerCapabilities.Prompts(listChanged = true),
            )
        )
    )

    server.addKonvoPromptCollection()

    return server
}

fun runMcpServerUsingStdio() {
    val server = configureServer()
    val transport = StdioServerTransport(
        inputStream = System.`in`.asSource().buffered(),
        outputStream = System.out.asSink().buffered()
    )

    runBlocking {
        server.connect(transport)
        val done = Job()
        server.onClose {
            done.complete()
        }
        done.join()
    }
}

fun runSseMcpServerUsingKtorPlugin(port: Int): Unit = runBlocking {
    println("Starting sse server on port $port")
    println("Use inspector to connect to the http://localhost:$port/sse")

    embeddedServer(CIO, host = "0.0.0.0", port = port) {
        mcp { configureServer() }
    }.start(wait = true)
}
