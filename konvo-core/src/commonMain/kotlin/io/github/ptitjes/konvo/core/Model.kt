package io.github.ptitjes.konvo.core

import io.github.ptitjes.konvo.core.spi.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import kotlin.contracts.*

interface Model {
    suspend fun preload()
    suspend fun chat(
        context: List<ChatMessage>,
        vetoToolCalls: suspend (calls: List<VetoableToolCall>) -> Unit = {},
    ): List<ChatMessage>
}

interface ModelBuilder {
    fun tools(tools: List<Tool>)
}

@OptIn(ExperimentalContracts::class)
fun buildModel(
    modelCard: ModelCard,
    builderAction: ModelBuilder.() -> Unit = {},
): Model {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }

    class ModelBuilderImpl : ModelBuilder {
        var tools: List<Tool>? = null

        override fun tools(
            tools: List<Tool>,
        ) {
            this.tools = tools
        }

        fun build(): DefaultModel {
            return DefaultModel(
                modelCard = modelCard,
                tools = tools,
            )
        }
    }

    return ModelBuilderImpl().apply(builderAction).build()
}

private class DefaultModel(
    private val modelCard: ModelCard,
    private val tools: List<Tool>?,
) : Model {
    override suspend fun preload() {
        modelCard.provider.preloadModel(modelCard)
    }

    override suspend fun chat(
        context: List<ChatMessage>,
        vetoToolCalls: suspend (calls: List<VetoableToolCall>) -> Unit,
    ): List<ChatMessage> {
        val outgoingMessages = mutableListOf<ChatMessage>()

        do {
            val newMessages = modelCard.provider.chat(modelCard, context + outgoingMessages, tools)

            var hadToolCalls = false
            for (message in newMessages) {
                if (message !is ChatMessage.Assistant) continue

                outgoingMessages.add(message)

                val toolCalls = message.toolCalls
                if (toolCalls != null) {
                    hadToolCalls = true

                    val pendingCalls = toolCalls
                        .toPendingCalls()
                        .validate()
                        .waitForPermissions(vetoToolCalls)
                        .execute()

                    for (pendingCall in pendingCalls) {
                        if (pendingCall.result == null) error("Invalid state")
                        val toolMessage = ChatMessage.Tool(call = pendingCall.original, result = pendingCall.result)
                        outgoingMessages.add(toolMessage)
                    }
                }
            }
        } while (hadToolCalls)

        return outgoingMessages
    }

    private data class PendingCall(
        val original: ToolCall,
        val resolvedTool: Tool? = null,
        val result: ToolCallResult? = null,
    )

    private fun List<ToolCall>.toPendingCalls(): List<PendingCall> = map { PendingCall(it) }

    private fun List<PendingCall>.validate(): List<PendingCall> = map { it.validate() }

    private fun PendingCall.validate(): PendingCall {
        val tool = tools?.find { it.name == original.name }

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
