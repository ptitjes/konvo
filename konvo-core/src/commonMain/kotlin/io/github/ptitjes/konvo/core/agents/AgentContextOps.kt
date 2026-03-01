package io.github.ptitjes.konvo.core.agents

import ai.koog.prompt.dsl.*
import io.github.ptitjes.konvo.core.mcp.*

context(context: AgentContext)
val prompt: Prompt get() = context.prompt

context(context: AgentContext)
fun resetPrompt(prompt: Prompt) = context.resetPrompt(prompt)

context(context: AgentContext)
fun resetPrompt(builder: PromptBuilder.() -> Unit) = context.resetPrompt(builder)

context(context: AgentContext)
fun appendToPrompt(builder: PromptBuilder.() -> Unit) = context.appendToPrompt(builder)

suspend context(context: AgentContext)
fun <T> withMcpSession(block: suspend (McpHostSession) -> T) = context.withMcpSession(block)
