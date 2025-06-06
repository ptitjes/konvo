package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.agents.core.agent.*
import ai.koog.agents.core.agent.config.*
import ai.koog.agents.core.agent.entity.*
import ai.koog.agents.core.feature.*
import ai.koog.agents.core.tools.*
import ai.koog.agents.features.common.config.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.model.*
import ai.koog.prompt.llm.*

class ChatAgent(
    private val initialPrompt: Prompt,
    private val model: LLModel,
    val maxAgentIterations: Int = 50,
    val promptExecutor: PromptExecutor,
    private val strategy: AIAgentStrategy,
    private val initialToolRegistry: ToolRegistry = ToolRegistry.Companion.EMPTY,
    private val installFeatures: AIAgent.FeatureContext.() -> Unit = {}
) {
    var toolRegistry: ToolRegistry = initialToolRegistry
    var prompt: Prompt = initialPrompt

    fun resetPrompt() {
        prompt = initialPrompt
    }

    fun resetToolRegistry() {
        toolRegistry = initialToolRegistry
    }

    fun updatePrompt(newPrompt: Prompt) {
        prompt = newPrompt
    }

    suspend fun runAndGetResult(input: String): String? {
        val agent = buildAgent()
        return agent.runAndGetResult(input)
    }

    private fun buildAgent(): AIAgent {
        val agentConfig = AIAgentConfig(
            prompt = prompt,
            model = model,
            maxAgentIterations = maxAgentIterations,
        )

        return AIAgent(
            promptExecutor = promptExecutor,
            strategy = strategy,
            agentConfig = agentConfig,
            toolRegistry = toolRegistry,
            installFeatures = {
                install(PromptCollector) {
                    collectPrompt = ::updatePrompt
                }
                installFeatures()
            },
        )
    }
}

private class PromptCollector {
    companion object Feature : AIAgentFeature<PromptCollectorConfig, PromptCollector> {
        override val key: AIAgentStorageKey<PromptCollector> =
            AIAgentStorageKey("agents-features-prompt-collector")

        override fun createInitialConfig(): PromptCollectorConfig = PromptCollectorConfig()

        override fun install(config: PromptCollectorConfig, pipeline: AIAgentPipeline) {
            val featureImpl = PromptCollector()

            pipeline.interceptAfterNode(this, featureImpl) { node, context, input, output ->
                config.collectPrompt(context.llm.readSession { prompt })
            }
        }
    }
}

private class PromptCollectorConfig : FeatureConfig() {
    var collectPrompt: (Prompt) -> Unit = {}
}
