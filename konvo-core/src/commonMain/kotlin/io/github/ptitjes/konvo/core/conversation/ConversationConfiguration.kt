package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.spi.*

data class ConversationConfiguration(
    val agent: ConversationAgentConfiguration,
)

sealed interface ConversationAgentConfiguration

data class QuestionAnswerAgentConfiguration(
    val prompt: PromptCard,
    val tools: List<ToolCard>,
    val model: ModelCard,
) : ConversationAgentConfiguration

data class RoleplayingAgentConfiguration(
    val character: Character,
    val characterGreetingIndex: Int?,
    val userName: String,
    val model: ModelCard,
) : ConversationAgentConfiguration
