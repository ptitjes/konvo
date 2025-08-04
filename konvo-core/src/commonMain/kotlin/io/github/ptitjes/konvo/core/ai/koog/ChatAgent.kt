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
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.core.conversation.*
import kotlinx.datetime.*

class ChatAgent(
    private val systemPrompt: Prompt,
    private val initialAssistantMessage: String? = null,
    private val model: LLModel,
    val maxAgentIterations: Int = 50,
    val promptExecutor: PromptExecutor,
    private val strategy: (ConversationAgentView) -> AIAgentStrategy<Message.User, List<Message.Assistant>>,
    private val initialToolRegistry: ToolRegistry = ToolRegistry.EMPTY,
    private val installFeatures: AIAgent.FeatureContext.(ConversationAgentView) -> Unit = {}
) {
    var toolRegistry: ToolRegistry = initialToolRegistry
    var prompt: Prompt = buildInitialPrompt()

    fun resetPrompt() {
        prompt = buildInitialPrompt()
    }

    private fun buildInitialPrompt(): Prompt {
        return prompt(systemPrompt) {
            initialAssistantMessage?.let { assistant { text(it) } }
        }
    }

    fun resetToolRegistry() {
        toolRegistry = initialToolRegistry
    }

    fun updatePrompt(newPrompt: Prompt) {
        prompt = newPrompt
    }

    private fun buildAgent(conversation: ConversationAgentView): AIAgent<Message.User, List<Message.Assistant>> {
        val agentConfig = AIAgentConfig(
            prompt = prompt,
            model = model,
            maxAgentIterations = maxAgentIterations,
        )

        return AIAgent(
            promptExecutor = promptExecutor,
            strategy = strategy(conversation),
            agentConfig = agentConfig,
            toolRegistry = toolRegistry,
            installFeatures = {
                install(PromptCollector) {
                    collectPrompt = ::updatePrompt
                }
                installFeatures(conversation)
            },
        )
    }

    suspend fun joinConversation(conversation: ConversationAgentView) {
        initialAssistantMessage?.let {
            conversation.sendAssistantEvent(AssistantEvent.Message(it))
        }

        conversation.userEvents.collect { userEvent ->
            conversation.sendAssistantEvent(AssistantEvent.Processing)
            when (userEvent) {
                is UserEvent.Message -> {
                    val agent = buildAgent(conversation)
                    val result = agent.run(userEvent.toUserMessage())
                    result.forEach { conversation.sendAssistantEvent(it.toAssistantEventMessage()) }
                }
            }
        }
    }

    private val clock = Clock.System

    private fun UserEvent.Message.toUserMessage(): Message.User =
        Message.User(content, attachments = attachments, metaInfo = RequestMetaInfo.create(clock))

    private fun Message.Assistant.toAssistantEventMessage(): AssistantEvent.Message =
        AssistantEvent.Message(content)
}

private class PromptCollector {
    companion object Feature : AIAgentFeature<PromptCollectorConfig, PromptCollector> {
        override val key: AIAgentStorageKey<PromptCollector> =
            AIAgentStorageKey("agents-features-prompt-collector")

        override fun createInitialConfig(): PromptCollectorConfig = PromptCollectorConfig()

        override fun install(config: PromptCollectorConfig, pipeline: AIAgentPipeline) {
            val featureImpl = PromptCollector()
            val interceptContext = InterceptContext(this, featureImpl)

            pipeline.interceptAfterNode(interceptContext) { eventContext ->
                config.collectPrompt(eventContext.context.llm.readSession { prompt })
            }
        }
    }
}

private class PromptCollectorConfig : FeatureConfig() {
    var collectPrompt: (Prompt) -> Unit = {}
}
