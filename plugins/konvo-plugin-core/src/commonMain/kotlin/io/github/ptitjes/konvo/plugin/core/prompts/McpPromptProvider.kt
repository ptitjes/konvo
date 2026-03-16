package io.github.ptitjes.konvo.plugin.core.prompts

import ai.koog.prompt.dsl.*
import io.github.ptitjes.konvo.plugin.core.mcp.*
import io.modelcontextprotocol.kotlin.sdk.client.*
import io.modelcontextprotocol.kotlin.sdk.types.*
import io.modelcontextprotocol.kotlin.sdk.types.Prompt

class McpPromptProvider(
    private val serversManager: McpServersManager,
) : PromptProvider {
    override val name: String = "MCP"

    override suspend fun query(): List<PromptCard> {
        return serversManager.clients.flatMap { (clientName, client) ->
            val serverCapabilities = client.serverCapabilities
            if (serverCapabilities == null || serverCapabilities.prompts == null) return@flatMap emptyList()

            client.listPrompts().prompts.map { prompt ->
                McpPromptCard(
                    clientName = clientName,
                    client = client,
                    prompt = prompt,
                )
            }
        }
    }

    private class McpPromptCard(
        val clientName: String,
        val client: Client,
        val prompt: Prompt,
    ) : PromptCard {
        override val name: String get() = prompt.name
        override val description: String? get() = prompt.description

        override suspend fun toPrompt(): ai.koog.prompt.dsl.Prompt {
            val promptResult = client.getPrompt(
                GetPromptRequest(GetPromptRequestParams(name, mapOf())),
            )

            return prompt(name) {
                promptResult.messages.forEach { message ->
                    val content = message.content

                    when (message.role) {
                        Role.User -> user {
                            when (content) {
                                is TextContent -> text(content.text)
                                is ImageContent -> TODO()
                                is AudioContent -> TODO()
                                is EmbeddedResource -> TODO()
                                is ResourceLink -> TODO()
                            }
                        }

                        Role.Assistant -> assistant {
                            when (content) {
                                is TextContent -> text(content.text)
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
