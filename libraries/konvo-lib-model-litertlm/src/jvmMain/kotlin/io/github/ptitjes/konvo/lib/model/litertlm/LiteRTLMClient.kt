package io.github.ptitjes.konvo.lib.model.litertlm

import ai.koog.agents.core.tools.*
import ai.koog.agents.core.tools.serialization.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.clients.*
import ai.koog.prompt.executor.clients.openai.base.*
import ai.koog.prompt.llm.*
import ai.koog.prompt.message.*
import ai.koog.prompt.message.Message
import ai.koog.prompt.params.*
import ai.koog.prompt.streaming.*
import com.google.ai.edge.litertlm.*
import io.github.oshai.kotlinlogging.*
import kotlinx.coroutines.flow.*
import kotlinx.io.*
import kotlinx.io.files.*
import kotlinx.serialization.json.*
import kotlin.random.*
import kotlin.time.*
import kotlin.uuid.*
import com.google.ai.edge.litertlm.Message as LiteRTLMMessage

object LiteRTLMProvider : LLMProvider(id = "litertlm", display = "LiteRT-LM")

data class LiteRTLMClientSettings(
    val modelPathById: Map<String, String>,
    val engineCacheDirectory: String,
    val backend: Backend,
    val visionBackend: Backend? = null,
    val audioBackend: Backend? = null,
)

class LiteRTLMClient(
    private val settings: LiteRTLMClientSettings,
    private val toolDescriptorConverter: ToolDescriptorSchemaGenerator = OpenAICompatibleToolDescriptorSchemaGenerator(),
    private val clock: Clock = Clock.System,
    private val logger: KLogger = KotlinLogging.logger {},
) : LLMClient() {
    private var currentModel: LLModel? = null
    private var currentEngine: Engine? = null

    private fun engineForModel(model: LLModel): Engine {
        val engine = currentEngine

        if (engine != null && model == currentModel) return engine

        engine?.close()

        maybeCreateCacheDir(settings.engineCacheDirectory)

        val config = EngineConfig(
            modelPath = modelFilePathFor(model),
            backend = settings.backend,
            visionBackend = settings.visionBackend,
            audioBackend = settings.audioBackend,
            cacheDir = settings.engineCacheDirectory,
        )

        return Engine(config).also {
            it.initialize()
            currentEngine = it
            currentModel = model
        }
    }

    private fun maybeCreateCacheDir(directory: String) {
        SystemFileSystem.createDirectories(Path(directory), mustCreate = false)
    }

    override suspend fun execute(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>,
    ): List<Message.Response> {
        check(model.provider == LiteRTLMProvider)
        checkModelFileExists(model)

        val engine = engineForModel(model)

        val systemMessage = prompt.messages.first { it is Message.System }
        val otherMessages = prompt.messages.filter { it != systemMessage }
        val messagesButLast = otherMessages.dropLast(1)
        val lastMessage = otherMessages.last()

        val conversationConfig = ConversationConfig(
            systemInstruction = systemMessage.toLiteRTLMMessage().contents,
            initialMessages = messagesButLast.toLiteRTLMMessages(),
            tools = tools.map { tool(KoogToolWrapper(it, toolDescriptorConverter)) },
            samplerConfig = prompt.params.toLiteRTLMSamplerConfig(),
            automaticToolCalling = false,
        )

        return engine.createConversation(conversationConfig).use { conversation ->
            val responseMessage = conversation.sendMessage(lastMessage.toLiteRTLMMessage())
            responseMessage.toKoogMessageResponses()
        }
    }

    override fun executeStreaming(
        prompt: Prompt,
        model: LLModel,
        tools: List<ToolDescriptor>,
    ): Flow<StreamFrame> {
        TODO()
    }

    private fun checkModelFileExists(model: LLModel) {
        val modelFilePath = modelFilePathFor(model)
        check(SystemFileSystem.exists(Path(modelFilePath))) {
            "Model file path '$modelFilePath' does not exist"
        }
    }

    private fun modelFilePathFor(model: LLModel): String {
        return (settings.modelPathById[model.id]
            ?: error("Model file path not found for model: ${model.id}"))
    }

    override suspend fun moderate(prompt: Prompt, model: LLModel): ModerationResult {
        error("Moderation is not supported by LiteRT-LM")
    }

    override fun llmProvider(): LLMProvider = LiteRTLMProvider

    override fun close() {
        currentEngine?.close()
    }

    private fun LiteRTLMMessage.toKoogMessageResponses(): List<Message.Response> {
        if (this.role != Role.MODEL) {
            error("Unsupported response role: $this")
        }

        return if (toolCalls.isNotEmpty()) {
            toolCalls.map { toolCall ->
                Message.Tool.Call(
                    id = Uuid.random().toString(),
                    tool = toolCall.name,
                    parts = listOf(
                        ContentPart.Text(
                            Json.encodeToString(
                                buildJsonObject {
                                    put("name", JsonPrimitive(toolCall.name))
                                    putJsonObject("parameters") {
                                        toolCall.arguments.forEach { (name, value) ->
                                            put(name, value.argumentValueToJson())
                                        }
                                    }
                                }
                            )
                        ),
                    ),
                    metaInfo = ResponseMetaInfo.create(clock),
                )
            }
        } else listOf(
            Message.Assistant(
                parts = this.contents.toKoogMessageParts(),
                metaInfo = ResponseMetaInfo.create(clock),
            )
        )
    }

    private fun Contents.toKoogMessageParts(): List<ContentPart.Text> = contents.map { content ->
        when (content) {
            is Content.Text -> ContentPart.Text(content.text)
            else -> error("Unsupported content type: $content")
        }
    }

    private fun Any?.argumentValueToJson(): JsonElement {
        return when (this) {
            null -> JsonNull
            is String -> JsonPrimitive(this)
            is Number -> JsonPrimitive(this)
            is Boolean -> JsonPrimitive(this)
            is List<*> -> JsonArray(this.map { it.argumentValueToJson() })
            is Map<*, *> -> JsonObject(this.map { (k, v) ->
                k.toString() to v.argumentValueToJson()
            }.toMap())

            else -> error("Unsupported argument value type: $this")
        }
    }

    private fun List<Message>.toLiteRTLMMessages(): List<LiteRTLMMessage> =
        map { message -> message.toLiteRTLMMessage() }

    private fun Message.toLiteRTLMMessage(): LiteRTLMMessage {
        return when (this) {
            is Message.System -> LiteRTLMMessage.system(
                contents = this.parts.toLiteRTLMContents(),
            )

            is Message.User -> LiteRTLMMessage.user(
                contents = this.parts.toLiteRTLMContents(),
            )

            is Message.Assistant -> LiteRTLMMessage.model(
                contents = this.parts.toLiteRTLMContents(),
            )

            is Message.Reasoning -> LiteRTLMMessage.model(
                contents = this.parts.toLiteRTLMContents(),
            )

            is Message.Tool.Call -> LiteRTLMMessage.model(
                toolCalls = listOf(
                    ToolCall(
                        name = this.tool,
                        arguments = this.contentJson["parameters"]?.jsonObject
                            ?: error("Tool call content is missing 'parameters' field"),
                    )
                ),
            )

            is Message.Tool.Result -> LiteRTLMMessage.tool(
                contents = this.parts.toLiteRTLMContents(),
            )
        }
    }

    private fun List<ContentPart>.toLiteRTLMContents(): Contents = Contents.of(
        this.map { part -> part.toLiteRTLMContent() }
    )

    private fun ContentPart.toLiteRTLMContent(): Content {
        return when (this) {
            is ContentPart.Text -> Content.Text(text)
            is ContentPart.Image -> content.buildLiteRTLMContent(
                withBytes = { Content.ImageBytes(it) },
                withFilePath = { Content.ImageFile(it) },
            )

            is ContentPart.Audio -> content.buildLiteRTLMContent(
                withBytes = { Content.AudioBytes(it) },
                withFilePath = { Content.AudioFile(it) },
            )

            is ContentPart.File -> content.buildLiteRTLMContent(
                withPlainText = { Content.Text(it) },
                withBytes = { Content.Text(it.decodeToString()) },
                withFilePath = { Content.Text(readTextFile(it)) },
            )

            else -> error("Unsupported content part: $this")
        }
    }

    private fun AttachmentContent.buildLiteRTLMContent(
        withBytes: (ByteArray) -> Content = error("Unsupported attachment content: $this"),
        withFilePath: (String) -> Content = error("Unsupported attachment content: $this"),
        withPlainText: (String) -> Content = error("Unsupported attachment content: $this"),
    ): Content = when (this) {
        is AttachmentContent.PlainText -> withPlainText(text)
        is AttachmentContent.Binary -> withBytes(asBytes())
        is AttachmentContent.URL if (isFileUrl()) -> withFilePath(fileUrlToAbsolutePath())
        else -> error("Unsupported attachment content: $this")
    }

    private fun LLMParams.toLiteRTLMSamplerConfig(): SamplerConfig = SamplerConfig(
        topK = 1,
        topP = 0.95,
        temperature = temperature ?: 0.8,
        seed = Random.nextInt(),
    )
}

private fun AttachmentContent.URL.isFileUrl(): Boolean {
    return url.startsWith("file:")
}

private fun AttachmentContent.URL.fileUrlToAbsolutePath(): String {
    val path = url.removePrefix("file:")
    val absolutePath = SystemFileSystem.resolve(Path(path)).toString()
    return absolutePath
}

private fun readTextFile(string: String): String {
    val source = SystemFileSystem.source(Path(string))
    return source.buffered().readString()
}

private class KoogToolWrapper(
    private val tool: ToolDescriptor,
    private val toolDescriptorConverter: ToolDescriptorSchemaGenerator,
) : OpenApiTool {
    override fun getToolDescriptionJsonString(): String {
        val arguments = toolDescriptorConverter.generate(tool)
        val description = buildJsonObject {
            put("name", tool.name)
            put("description", tool.description)
            put("parameters", arguments)
        }
        return Json.encodeToString(description)
            .also { println("Generated tool description: $it") }
    }

    override fun execute(paramsJsonString: String): String {
        error("Automatic tool calling is not supported")
    }
}
