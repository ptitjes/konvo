package io.github.ptitjes.konvo.plugin.core.agents.toolkit

import ai.koog.agents.core.agent.config.*
import ai.koog.agents.core.agent.context.*
import ai.koog.agents.core.agent.entity.*
import ai.koog.agents.core.feature.*
import ai.koog.agents.core.feature.config.*
import ai.koog.agents.core.feature.pipeline.*
import ai.koog.prompt.dsl.*
import io.github.ptitjes.konvo.plugin.core.tools.*

// The interaction feature is specific to an interaction
class InteractionFeature(
    val controller: InteractionScope<*>,
    val tools: List<ToolCard>,
) : InteractionScope<Any> by controller.asUnsafeScopeOfAny() {

    class Config : FeatureConfig() {
        var agentController: () -> InteractionScope<*> = { error("Not initialized") }
        var promptCollector: (Prompt) -> Unit = {}
        var tools: List<ToolCard> = emptyList()
    }

    companion object Feature :
        AIAgentGraphFeature<Config, InteractionFeature>,
        AIAgentFunctionalFeature<Config, InteractionFeature> {

        override val key: AIAgentStorageKey<InteractionFeature> = AIAgentStorageKey("agents-features-interaction")

        override fun createInitialConfig(agentConfig: AIAgentConfig): Config = Config()

        override fun install(config: Config, pipeline: AIAgentGraphPipeline): InteractionFeature {
            val feature = InteractionFeature(
                controller = config.agentController(),
                tools = config.tools,
            )

            pipeline.interceptLLMCallCompleted(this) { eventContext ->
                config.promptCollector(
                    eventContext.prompt.withMessages {
                        it + eventContext.responses
                    }
                )
            }

            return feature
        }

        override fun install(config: Config, pipeline: AIAgentFunctionalPipeline): InteractionFeature {
            val feature = InteractionFeature(
                controller = config.agentController(),
                tools = config.tools,
            )

            // Register interceptors
            pipeline.interceptLLMCallCompleted(this) { eventContext ->
                config.promptCollector(
                    eventContext.prompt.withMessages {
                        it + eventContext.responses
                    }
                )
            }

            return feature
        }
    }
}

@Suppress("UNCHECKED_CAST")
private fun InteractionScope<*>.asUnsafeScopeOfAny(): InteractionScope<Any> = this as InteractionScope<Any>

/**
 * Extension function to access the interaction feature from an agent context.
 *
 * @return The [InteractionFeature] feature instance for this agent
 * @throws IllegalStateException if the interaction feature is not installed
 */
val AIAgentContext.interaction: InteractionFeature get() = featureOrThrow(InteractionFeature)

/**
 * Executes the provided action within the context of the AI agent's interaction layer.
 *
 * @param action A suspendable lambda function that receives the [InteractionFeature] instance and the current [AIAgentContext]
 *               as its parameters. This allows custom logic that interacts with the interaction layer to be executed.
 * @return A result of type [T] produced by the execution of the provided action.
 */
suspend fun <T> AIAgentContext.withInteractionFeature(
    action: suspend InteractionFeature.(AIAgentContext) -> T,
): T = this.interaction.action(this)
