package io.github.ptitjes.konvo.core.ai.base

import io.github.ptitjes.konvo.core.ai.spi.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import kotlin.contracts.*

interface ChatBot {
    suspend fun chat(
        memoryId: Any,
        userMessage: ChatMessage.User,
        vetoToolCalls: suspend (calls: List<VetoableToolCall>) -> Unit = {},
    ): Flow<ChatMessage>

    fun getChatMemory(memoryId: Any): ChatMemory?

    fun evictChatMemory(memoryId: Any): Boolean
}

interface ChatBotBuilder {
    fun chatMemory(memoryProvider: ChatMemoryProvider)
    fun prompt(promptProvider: PromptProvider)
    fun tools(toolSelector: ToolSelector)
}

typealias ChatMemoryProvider = ChatMemoryProviderScope.() -> ChatMemory

interface ChatMemoryProviderScope {
    val memoryId: Any
}

typealias PromptProvider = PromptProviderScope.() -> List<ChatMessage>

interface PromptProviderScope {
    val memoryId: Any
    val userMessage: ChatMessage.User
}

typealias ToolSelector = ToolSelectorScope.() -> List<Tool>?

interface ToolSelectorScope {
    val memoryId: Any
    val userMessage: ChatMessage.User
}

@OptIn(ExperimentalContracts::class)
fun ChatBot(
    modelCard: ModelCard,
    builderAction: ChatBotBuilder.() -> Unit = {},
): ChatBot {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    val chatModel = modelCard.provider.newChatModel(modelCard)
    return ChatBot(chatModel, builderAction)
}

@OptIn(ExperimentalContracts::class)
fun ChatBot(
    model: ChatModel,
    builderAction: ChatBotBuilder.() -> Unit = {},
): ChatBot {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    class ChatBotBuilderImpl : ChatBotBuilder {
        var memoryProvider: ChatMemoryProvider? = null
        var promptProvider: PromptProvider? = null
        var toolSelector: ToolSelector? = null

        override fun chatMemory(memoryProvider: ChatMemoryProvider) {
            this.memoryProvider = memoryProvider
        }

        override fun prompt(promptProvider: PromptProvider) {
            this.promptProvider = promptProvider
        }

        override fun tools(toolSelector: ToolSelector) {
            this.toolSelector = toolSelector
        }

        fun build(): DefaultChatBot {
            return DefaultChatBot(
                model = model,
                memoryProvider = memoryProvider ?: { ObliviousChatMemory(memoryId) },
                promptProvider = promptProvider ?: { listOf() },
                toolSelector = toolSelector ?: { null }
            )
        }
    }

    return ChatBotBuilderImpl().apply(builderAction).build()
}

private class DefaultChatBot(
    private val model: ChatModel,
    private val memoryProvider: ChatMemoryProvider,
    private var promptProvider: PromptProvider,
    private val toolSelector: ToolSelector,
) : ChatBot {
    override suspend fun chat(
        memoryId: Any,
        userMessage: ChatMessage.User,
        vetoToolCalls: suspend (calls: List<VetoableToolCall>) -> Unit,
    ): Flow<ChatMessage> = flow {
        val scope = ChatBotScope(memoryId, userMessage)

        val memory = getChatMemory(memoryId) ?: scope.memoryProvider().also { memory ->
            chatMemories.put(memoryId, memory)

            val prompt = scope.promptProvider()
            for (message in prompt) {
                memory.add(model.withTokenCount(message))
            }
        }

        val tools = scope.toolSelector()

        memory.add(model.withTokenCount(userMessage))

        do {
            val assistantMessage = model.chat(memory.messages, tools)

            memory.add(assistantMessage)
            emit(assistantMessage)

            var hadToolCalls = false
            val toolCalls = assistantMessage.toolCalls
            if (toolCalls != null) {
                if (tools == null) error("Received a tool call, but no tools were given")
                hadToolCalls = true

                val pendingCalls = toolCalls
                    .toPendingCalls()
                    .validate(tools)
                    .waitForPermissions(vetoToolCalls)
                    .execute()

                for (pendingCall in pendingCalls) {
                    if (pendingCall.result == null) error("Invalid state")
                    val toolMessage = ChatMessage.Tool(call = pendingCall.original, result = pendingCall.result)

                    memory.add(model.withTokenCount(toolMessage))
                    emit(toolMessage)
                }
            }
        } while (hadToolCalls)
    }

    private class ChatBotScope(
        override val memoryId: Any,
        override val userMessage: ChatMessage.User,
    ) : ChatMemoryProviderScope, PromptProviderScope, ToolSelectorScope

    private val chatMemories = mutableMapOf<Any, ChatMemory>()

    override fun getChatMemory(memoryId: Any): ChatMemory? = chatMemories[memoryId]
    override fun evictChatMemory(memoryId: Any): Boolean = chatMemories.remove(memoryId) != null

    private data class PendingCall(
        val original: ToolCall,
        val resolvedTool: Tool? = null,
        val result: ToolCallResult? = null,
    )

    private fun List<ToolCall>.toPendingCalls(): List<PendingCall> = map { PendingCall(it) }

    private fun List<PendingCall>.validate(tools: List<Tool>): List<PendingCall> = map { it.validate(tools) }

    private fun PendingCall.validate(tools: List<Tool>): PendingCall {
        val tool = tools.find { it.name == original.name }

        // TODO sanitize and validate arguments

        return when {
            tool == null -> copy(result = ToolCallResult.NoSuchTool)
            else -> copy(resolvedTool = tool)
        }
    }

    private suspend fun List<PendingCall>.waitForPermissions(
        vetoToolCalls: suspend (calls: List<VetoableToolCall>) -> Unit,
    ): List<PendingCall> {
        val result = toMutableList()
        val allDone = CompletableDeferred<Unit>()

        lateinit var remaining: MutableList<VetoableCall>
        val callback = { index: Int, call: VetoableCall, allowed: Boolean ->
            if (!allowed) result[index] = result[index].copy(result = ToolCallResult.NotAllowed)
            remaining.remove(call)
            if (remaining.isEmpty()) {
                allDone.complete(Unit)
            }
        }

        val vetoableCalls = mapIndexedNotNull { index, call ->
            if (call.result != null) return@mapIndexedNotNull null
            if (call.resolvedTool == null) return@mapIndexedNotNull null
            if (!call.resolvedTool.askPermission) return@mapIndexedNotNull null
            VetoableCall(index, callback, call.resolvedTool, call.original.arguments)
        }

        if (vetoableCalls.isEmpty()) return this

        @Suppress("AssignedValueIsNeverRead")
        remaining = vetoableCalls.toMutableList()

        vetoToolCalls.invoke(vetoableCalls)

        allDone.await()

        return result
    }

    private suspend fun List<PendingCall>.execute(): List<PendingCall> = map { it.execute() }

    private suspend fun PendingCall.execute(): PendingCall {
        val tool = resolvedTool
        if (result != null || tool == null) return this

        val executionResult = runCatching {
            ToolCallResult.Success(tool.evaluator.invoke(original.arguments))
        }.getOrElse { ToolCallResult.ExecutionFailure("Tool call failed: ${it.message}") }

        return copy(result = executionResult)
    }

    private class VetoableCall(
        val index: Int,
        val callback: (index: Int, call: VetoableCall, allowed: Boolean) -> Unit,
        override val tool: Tool,
        override val arguments: Map<String, JsonElement>,
    ) : VetoableToolCall {
        override fun allow() = callback(index, this, true)
        override fun reject() = callback(index, this, false)
    }
}
