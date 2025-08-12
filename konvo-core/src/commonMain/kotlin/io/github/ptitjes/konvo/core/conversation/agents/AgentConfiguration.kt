package io.github.ptitjes.konvo.core.conversation.agents

import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.spi.*

sealed interface AgentConfiguration

/** Default no-op configuration used when an agent is not specified or cannot be resolved. */
object NoAgentConfiguration : AgentConfiguration

data class QuestionAnswerAgentConfiguration(
    val prompt: PromptCard,
    val tools: List<ToolCard>,
    val model: ModelCard,
) : AgentConfiguration

data class RoleplayAgentConfiguration(
    val character: CharacterCard,
    val characterGreetingIndex: Int?,
    val userName: String,
    val model: ModelCard,
) : AgentConfiguration

suspend fun AgentConfiguration.buildAgent(): ChatAgent = when (this) {
    is QuestionAnswerAgentConfiguration -> buildQuestionAnswerAgent(this)
    is RoleplayAgentConfiguration -> buildRoleplayAgent(this)
    is NoAgentConfiguration -> error("No agent configured for this conversation")
}
