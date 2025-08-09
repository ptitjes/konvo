package io.github.ptitjes.konvo.core.ai.mcp

import io.github.oshai.kotlinlogging.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.sse.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import io.modelcontextprotocol.kotlin.sdk.shared.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.io.*
import kotlinx.io.files.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import kotlin.concurrent.atomics.*
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
                is TransportSpecification.Stdio if (name == "web") -> CustomStdioClientTransport(
                    input = process!!.inputStream.asSource().log(name).buffered(),
                    output = process!!.outputStream.asSink().log(name).buffered()
                )
                is TransportSpecification.Stdio -> StdioClientTransport(
                    input = process!!.inputStream.asSource().log(name).buffered(),
                    output = process!!.outputStream.asSink().log(name).buffered()
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

fun RawSource.log(name: String): RawSource = LoggerSource(this, name)

class LoggerSource(private val delegate: RawSource, name: String) : RawSource {
    private val log = SystemFileSystem.sink(Path("./$name-input-client.log"))

    override fun readAtMostTo(sink: Buffer, byteCount: Long): Long {
        val buffer = Buffer()
        val read = delegate.readAtMostTo(buffer, byteCount)
        if (read == -1L) return -1L
        log.write(buffer.copy(), read)
        log.flush()
        buffer.readAtMostTo(sink, read)
        return read
    }

    override fun close() {
        val closedBuffer = Buffer().apply { writeString("CLOSED\n") }
        log.write(closedBuffer, closedBuffer.size)
        log.flush()
        log.close()
        delegate.close()
    }
}

fun RawSink.log(name: String): RawSink = LoggerSink(this, name)

class LoggerSink(private val delegate: RawSink, name: String) : RawSink {
    private val log = SystemFileSystem.sink(Path("./$name-output-client.log"))

    override fun write(source: Buffer, byteCount: Long) {
        log.write(source.copy(), byteCount)
        log.flush()
        delegate.write(source, byteCount)
    }

    override fun flush() {
        delegate.flush()
    }

    override fun close() {
        log.close()
        delegate.close()
    }
}

@OptIn(ExperimentalAtomicApi::class)
public class CustomStdioClientTransport(
    private val input: Source,
    private val output: Sink
) : AbstractTransport() {
    private val logger = KotlinLogging.logger {}
    private val ioCoroutineContext: CoroutineContext = Dispatchers.IO
    private val scope by lazy {
        CoroutineScope(ioCoroutineContext + SupervisorJob())
    }
    private var job: Job? = null
    private val initialized: AtomicBoolean = AtomicBoolean(false)
    private val sendChannel = Channel<JSONRPCMessage>(Channel.UNLIMITED)
    private val readBuffer = ReadBuffer()

    override suspend fun start() {
        if (!initialized.compareAndSet(false, true)) {
            error("StdioClientTransport already started!")
        }

        logger.debug { "Starting StdioClientTransport..." }

        val outputStream = output.buffered()

        job = scope.launch(CoroutineName("StdioClientTransport.IO#${hashCode()}")) {
            val readJob = launch {
                logger.debug { "Read coroutine started." }
                try {
                    input.use {
                        logger.debug { "----------------- 1" }
                        while (isActive) {
                            logger.debug { "----------------- 2" }
                            val buffer = Buffer()
                            val bytesRead = input.readAtMostTo(buffer, 8192)
                            logger.debug { "----------------- 3 > $bytesRead" }
                            if (bytesRead == -1L) break
                            if (bytesRead > 0L) {
                                logger.debug { "----------------- 4" }
                                readBuffer.append(buffer.readByteArray())
                                logger.debug { "----------------- 5" }
                                processReadBuffer()
                                logger.debug { "----------------- 6" }
                            }
                            logger.debug { "----------------- 7" }
                        }
                        logger.debug { "----------------- 8" }
                    }
                } catch (e: Exception) {
                    _onError.invoke(e)
                    logger.error(e) { "Error reading from input stream" }
                }
            }

            val writeJob = launch {
                logger.debug { "Write coroutine started." }
                try {
                    sendChannel.consumeEach { message ->
                        val json = serializeMessage(message)
                        outputStream.writeString(json)
                        outputStream.flush()
                    }
                } catch (e: Throwable) {
                    if (isActive) {
                        _onError.invoke(e)
                        logger.error(e) { "Error writing to output stream" }
                    }
                } finally {
                    output.close()
                }
            }

            readJob.join()
            writeJob.cancelAndJoin()
            _onClose.invoke()
        }
    }

    override suspend fun send(message: JSONRPCMessage) {
        if (!initialized.load()) {
            error("Transport not started")
        }

        sendChannel.send(message)
    }

    override suspend fun close() {
        if (!initialized.compareAndSet(true, false)) {
            error("Transport is already closed")
        }
        job?.cancelAndJoin()
        input.close()
        output.close()
        readBuffer.clear()
        sendChannel.close()
        _onClose.invoke()
    }

    private suspend fun processReadBuffer() {
        logger.debug { "============== 1" }
        while (true) {
            logger.debug { "============== 2" }
            val msg = readBuffer.readMessage() ?: break
            logger.debug { "============== 3" }
            try {
                _onMessage.invoke(msg)
            } catch (e: Throwable) {
                _onError.invoke(e)
                logger.error(e) { "Error processing message." }
            }
            logger.debug { "============== 4" }
        }
        logger.debug { "============== 5" }
    }
}

internal fun serializeMessage(message: JSONRPCMessage): String {
    return McpJson.encodeToString(message) + "\n"
}

@OptIn(ExperimentalSerializationApi::class)
@PublishedApi
internal val McpJson: Json by lazy {
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
        classDiscriminatorMode = ClassDiscriminatorMode.NONE
        explicitNulls = false
    }
}
