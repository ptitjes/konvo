package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.spi.*

data class ConversationConfiguration(
    val mode: ConversationModeConfiguration,
)

sealed interface ConversationModeConfiguration

data class QuestionAnswerModeConfiguration(
    val prompt: PromptCard,
    val tools: List<ToolCard>,
    val model: ModelCard,
) : ConversationModeConfiguration

data class RoleplayingModeConfiguration(
    val model: ModelCard,
    val character: Character,
    val characterGreetingIndex: Int?,
    val userName: String,
) : ConversationModeConfiguration
