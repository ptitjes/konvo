package io.github.ptitjes.konvo.plugin.core.agents.toolkit

import ai.koog.agents.core.agent.*
import ai.koog.agents.core.agent.config.*
import ai.koog.agents.core.agent.context.*
import ai.koog.agents.core.agent.entity.*
import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.tools.*
import ai.koog.agents.features.eventHandler.feature.*
import ai.koog.prompt.executor.model.*
import ai.koog.prompt.llm.*
import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.*
import io.github.ptitjes.konvo.plugin.core.tools.*
import kotlinx.serialization.json.*
import kotlin.uuid.*

suspend context(context: AgentContext)
inline fun <reified I, reified O> InteractionScope<*>.runGraph(
    promptExecutor: PromptExecutor,
    model: LLModel,
    maxAgentIterations: Int,
    input: I,
    tools: List<ToolCard> = emptyList(),
    crossinline define: AIAgentSubgraphBuilderBase<I, O>.() -> Unit,
): O {
    val agent = buildAgent<I, O>(
        promptExecutor = promptExecutor,
        model = model,
        maxAgentIterations = maxAgentIterations,
        strategy = strategy<I, O>(name = "graph") { define() },
        tools = tools,
    )

    return agent.run(input)
}

suspend context(_: AgentContext)
inline fun <reified I, reified O> InteractionScope<*>.runFunction(
    promptExecutor: PromptExecutor,
    model: LLModel,
    maxAgentIterations: Int,
    input: I,
    crossinline define: AIAgentFunctionalContext.(I) -> O,
): O {
    val agent = buildAgent<I, O>(
        promptExecutor = promptExecutor,
        model = model,
        maxAgentIterations = maxAgentIterations,
        strategy = functionalStrategy("function") { define(it) },
    )

    return agent.run(input)
}

context(context: AgentContext)
inline fun <reified I, reified O> InteractionScope<*>.buildAgent(
    promptExecutor: PromptExecutor,
    model: LLModel,
    maxAgentIterations: Int,
    strategy: AIAgentGraphStrategy<I, O>,
    tools: List<ToolCard>,
): GraphAIAgent<I, O> = AIAgent<I, O>(
    promptExecutor = promptExecutor,
    agentConfig = AIAgentConfig(
        prompt = context.prompt,
        model = model,
        maxAgentIterations = maxAgentIterations,
    ),
    strategy = strategy,
    toolRegistry = tools.map { it.toTool() }.let { tools ->
        ToolRegistry {
            tools(tools)
        }
    },
) {
    install(InteractionFeature) {
        agentController = { this@buildAgent }
        promptCollector = { newPrompt -> context.resetPrompt(newPrompt) }
        this.tools = tools
    }

    install(EventHandler) {
        toolUsageNotifications(this@buildAgent)
    }
}

context(context: AgentContext)
inline fun <reified I, reified O> InteractionScope<*>.buildAgent(
    promptExecutor: PromptExecutor,
    model: LLModel,
    maxAgentIterations: Int,
    strategy: AIAgentFunctionalStrategy<I, O>,
): FunctionalAIAgent<I, O> = AIAgent(
    promptExecutor = promptExecutor,
    agentConfig = AIAgentConfig(
        prompt = context.prompt,
        model = model,
        maxAgentIterations = maxAgentIterations,
    ),
    strategy = strategy,
) {
    install(InteractionFeature) {
        agentController = { this@buildAgent }
        promptCollector = { newPrompt -> context.resetPrompt(newPrompt) }
    }

    install(EventHandler) {
        toolUsageNotifications(this@buildAgent)
    }
}

@PublishedApi
internal fun EventHandlerConfig.toolUsageNotifications(scope: InteractionScope<*>) {
    onToolValidationFailed { eventContext ->
        scope.act(
            ToolUsage.Notification(
                call = ToolUsage.Call(
                    id = eventContext.toolCallId ?: Uuid.random().toString(),
                    tool = eventContext.toolName,
                    arguments = eventContext.toolArgs,
                ),
                result = ToolUsage.CallResult.ExecutionFailure(eventContext.message),
            ),
        )
    }
    onToolCallCompleted { eventContext ->
        val result = eventContext.toolResult
        val structuredContent = (result as? JsonObject)?.get("structuredContent")

        scope.act(
            ToolUsage.Notification(
                call = ToolUsage.Call(
                    id = eventContext.toolCallId ?: Uuid.random().toString(),
                    tool = eventContext.toolName,
                    arguments = eventContext.toolArgs,
                ),
                result = ToolUsage.CallResult.Success(structuredContent ?: result),
            ),
        )
    }
    onToolCallFailed { eventContext ->
        scope.act(
            ToolUsage.Notification(
                call = ToolUsage.Call(
                    id = eventContext.toolCallId ?: Uuid.random().toString(),
                    tool = eventContext.toolName,
                    arguments = eventContext.toolArgs,
                ),
                result = ToolUsage.CallResult.ExecutionFailure(eventContext.message),
            ),
        )
    }
}
