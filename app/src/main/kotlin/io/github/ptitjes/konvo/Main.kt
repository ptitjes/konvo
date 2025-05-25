package io.github.ptitjes.konvo

import ai.koog.agents.core.agent.entity.*
import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.local.features.eventHandler.feature.*
import ai.koog.agents.mcp.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.llms.*
import ai.koog.prompt.executor.ollama.client.*
import ai.koog.prompt.message.*
import ai.koog.prompt.params.*
import io.github.ptitjes.konvo.backend.mcp.*
import io.github.ptitjes.konvo.core.koog.*
import kotlinx.io.files.*

private const val MODEL = "hf.co/mradermacher/ToolACE-2-Llama-3.1-8B-i1-GGUF:Q4_K_M"

suspend fun main() {
    val configuration = KonvoAppConfiguration.readConfiguration(Path("config/konvo.json"))
    val client = OllamaClient(configuration.ollama.url)
    val modelCard = client.getModelOrNull(MODEL) ?: error("Model not found")
    val model = modelCard.toLLModel()

    val mcpServersManager = McpServersManager(configuration.mcp.servers)

    try {
        mcpServersManager.startAndConnectServers()

        val toolRegistry = mcpServersManager.clients
            .map { (_, client) -> McpToolRegistryProvider.fromClient(client) }
            .reduce { a, b -> a + b }

        val agent = ChatAgent(
            initialPrompt = prompt(id = "qa", params = LLMParams(temperature = 0.8)) {
                system("You are a helpful assistant and an expert in function composition.")
            },
            model = model,
            maxAgentIterations = 50,
            promptExecutor = SingleLLMPromptExecutor(client),
            strategy = konvoQAStrategy { call ->
                println("Model wants to call tool: $call")
                println("Do you accept? (yes/no)")
                val answer = readln()
                answer == "yes"
            },
            initialToolRegistry = toolRegistry,
        ) {
            install(EventHandler) {
                onToolCall = { tool, args ->
                    println("Tool ${tool.name} is being called with $args")
                }
            }
        }

        while (true) {
            val input = readln()
            val result = agent.runAndGetResult(input)
            println(result)
        }
    } finally {
        mcpServersManager.closeServers()
    }
}

private fun konvoQAStrategy(
    vetToolCall: suspend (Message.Tool.Call) -> Boolean,
): AIAgentStrategy = strategy("qa") {
    val nodeCallLLM by nodeLLMRequest("sendInput")
    val nodeVetToolCall by node<Message.Tool.Call, VettedToolCall>("nodeVetToolCall") { call ->
        VettedToolCall(call, vetToolCall(call))
    }
    val nodeExecuteTool by nodeExecuteTool("nodeExecuteTool")
    val nodeSendToolResult by nodeLLMSendToolResult("nodeSendToolResult")

    edge(nodeStart forwardTo nodeCallLLM)

    edge(nodeCallLLM forwardTo nodeVetToolCall onToolCall { true })
    edge(nodeCallLLM forwardTo nodeFinish onAssistantMessage { true })

    edge(nodeVetToolCall forwardTo nodeExecuteTool onCondition { it.vetted } transformed { it.call })

    edge(nodeExecuteTool forwardTo nodeSendToolResult)

    edge(nodeSendToolResult forwardTo nodeVetToolCall onToolCall { true })
    edge(nodeSendToolResult forwardTo nodeFinish onAssistantMessage { true })
}

private data class VettedToolCall(
    val call: Message.Tool.Call,
    val vetted: Boolean,
)
