package io.github.ptitjes.konvo.core.ai.mcp

import ai.koog.prompt.dsl.*
import ai.koog.prompt.dsl.Prompt
import io.github.ptitjes.konvo.core.ai.prompts.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import io.modelcontextprotocol.kotlin.sdk.Prompt as McpPrompt

class McpPromptProvider(
    private val serversManager: McpServersManager,
) : PromptProvider {
    override val name: String = "MCP"

    override suspend fun query(): List<PromptCard> {
        return serversManager.clients.flatMap { (clientName, client) ->
            val serverCapabilities = client.serverCapabilities
            if (serverCapabilities == null || serverCapabilities.prompts == null) return@flatMap emptyList()

            client.listPrompts()?.prompts?.map { prompt ->
                McpPromptCard(
                    clientName = clientName,
                    client = client,
                    prompt = prompt,
                )
            } ?: emptyList()
        }
    }

    private inner class McpPromptCard(
        val clientName: String,
        val client: Client,
        val prompt: McpPrompt,
    ) : PromptCard {
        override val name: String get() = prompt.name
        override val description: String? get() = prompt.description

        override suspend fun toPrompt(): Prompt {
            val promptResult = client.getPrompt(GetPromptRequest(name, mapOf()))
                ?: error("Prompt not found")

            return prompt(name) {
                promptResult.messages.forEach { message ->
                    val content = message.content

                    when (message.role) {
                        Role.user -> user {
                            when (content) {
                                is TextContent -> content.text?.let { text(it) }
                                is ImageContent -> TODO()
//                                is AudioContent -> TODO()
                                is EmbeddedResource -> TODO()
                                is UnknownContent -> error("Unsupported content")
                            }
                        }

                        Role.assistant -> assistant {
                            when (content) {
                                is TextContent -> content.text?.let { text(it) }
                                is EmbeddedResource -> TODO()
                                else -> error("Unsupported content")
                            }
                        }
                    }
                }
            }
        }
    }
}
