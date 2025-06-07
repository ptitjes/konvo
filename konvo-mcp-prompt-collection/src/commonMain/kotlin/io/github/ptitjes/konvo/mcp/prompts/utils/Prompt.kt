package io.github.ptitjes.konvo.mcp.prompts.utils

import ai.koog.prompt.dsl.*
import ai.koog.prompt.message.*
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.Prompt
import io.modelcontextprotocol.kotlin.sdk.server.*
import ai.koog.prompt.dsl.Prompt as KoogPrompt

fun Server.addKoogPrompt(
    name: String,
    description: String? = null,
    arguments: List<PromptArgument>? = null,
    handler: PromptBuilder.(arguments: Map<String, String>?) -> Unit,
) {
    addPrompt(
        Prompt(
            name = name,
            description = description,
            arguments = arguments,
        )
    ) { request ->
        val prompt = prompt(name) {
            handler(request.arguments)
        }

        GetPromptResult(
            description = description,
            messages = prompt.toMcpPrompt(),
        )
    }
}

private fun KoogPrompt.toMcpPrompt(): List<PromptMessage> = messages.map { message ->
    when (message) {
        is Message.User -> PromptMessage(
            role = Role.user,
            content = TextContent(message.content),
        )

        is Message.Assistant -> PromptMessage(
            role = Role.assistant,
            content = TextContent(message.content),
        )

        else -> error("Unsupported message type")
    }
}
