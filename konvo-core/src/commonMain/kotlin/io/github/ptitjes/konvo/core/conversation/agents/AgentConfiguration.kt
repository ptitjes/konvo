package io.github.ptitjes.konvo.core.conversation.agents

import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.*

sealed interface AgentConfiguration

data class QuestionAnswerAgentConfiguration(
    val prompt: PromptCard,
    val tools: List<ToolCard>,
    val model: ModelCard,
) : AgentConfiguration

data class RoleplayingAgentConfiguration(
    val character: CharacterCard,
    val characterGreetingIndex: Int?,
    val userName: String,
    val model: ModelCard,
) : AgentConfiguration

suspend fun ConversationConfiguration.buildAgent(): ChatAgent = when (agent) {
    is QuestionAnswerAgentConfiguration -> buildQuestionAnswerAgent(agent)
    is RoleplayingAgentConfiguration -> buildRoleplayingAgent(agent)
}
