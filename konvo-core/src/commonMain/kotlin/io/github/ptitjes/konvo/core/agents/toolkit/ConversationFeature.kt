package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.agents.core.agent.*
import ai.koog.agents.core.agent.AIAgent.Companion.State.*
import ai.koog.agents.core.agent.context.*
import ai.koog.agents.core.agent.entity.*
import ai.koog.agents.core.annotation.*
import ai.koog.agents.core.feature.*
import ai.koog.agents.core.feature.pipeline.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.tools.*

class ConversationFeature(
    val view: InteractionDevice.Agent,
    val tools: List<ToolCard>,
) {
    companion object Feature : AIAgentGraphFeature<ConversationFeatureConfig, ConversationFeature> {
        override val key: AIAgentStorageKey<ConversationFeature> = AIAgentStorageKey("agents-features-conversation")

        override fun createInitialConfig(): ConversationFeatureConfig = ConversationFeatureConfig()

        override fun install(
            config: ConversationFeatureConfig,
            pipeline: AIAgentGraphPipeline,
        ): ConversationFeature {
            val conversationFeature = ConversationFeature(
                config.conversationViewProvider(),
                tools = config.tools,
            )
            return conversationFeature
        }
    }
}

/**
 * Extension function to access the conversation feature from an agent context.
 *
 * @return The [ConversationFeature] feature instance for this agent
 * @throws IllegalStateException if the conversation feature is not installed
 */
fun AIAgentContext.conversation(): ConversationFeature = featureOrThrow(ConversationFeature)

/**
 * Executes the provided action within the context of the AI agent's conversation layer.
 *
 * This function enhances agents with persistent state management capabilities by leveraging the [ConversationFeature component
 * within the current [AIAgentContext]. The supplied action is executed with the conversation layer, enabling operations
 * that require consistent and reliable state management across the lifecycle of the agent.
 *
 * @param action A suspendable lambda function that receives the [ConversationFeature] instance and the current [AIAgentContext]
 *               as its parameters. This allows custom logic that interacts with the conversation layer to be executed.
 * @return A result of type [T] produced by the execution of the provided action.
 */
suspend fun <T> AIAgentContext.withConversationFeature(
    action: suspend ConversationFeature.(AIAgentContext) -> T,
): T = this.conversation().action(this)

/**
 * Extension function to access the conversation feature from an agent.
 *
 * @return The [ConversationFeature] feature instance for this agent
 * @throws IllegalStateException if the conversation feature is not installed
 */
fun StatefulSingleUseAIAgent<*, *, *>.conversation(): ConversationFeature =
    featureOrThrow(ConversationFeature)

/**
 * Executes the provided action within the context of the agent's ConversationFeature layer if the agent is in a running state.
 *
 * This function allows interaction with the ConversationFeature mechanism associated with the agent, ensuring that
 * the operation is carried out in the correct execution context.
 *
 * @param action A suspending function defining operations to perform using the agent's ConversationFeature mechanism
 *               and the current agent context.
 * @return The result of the execution of the provided action.
 * @throws IllegalStateException If the agent is not in a running state when this function is called.
 */
@OptIn(InternalAgentsApi::class)
suspend fun <T> StatefulSingleUseAIAgent<*, *, *>.withConversationFeature(
    action: suspend ConversationFeature.(AIAgentContext) -> T,
): T = when (val state = getState()) {
    is Running<*> -> this.conversation().action(state.rootContext)
    else -> throw IllegalStateException("Agent is not running. Current agents's state: $state")
}
