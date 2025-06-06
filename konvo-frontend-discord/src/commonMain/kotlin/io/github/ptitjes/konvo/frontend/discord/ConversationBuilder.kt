package io.github.ptitjes.konvo.frontend.discord

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

data class ConversationBuilder(
    val mode: ConversationModeBuilder? = null,
    val endMessageBuilder: (EphemeralMessageBuilder.() -> Unit)? = null,
) {
    fun isValid(): Boolean = mode != null && mode.isValid()

    fun build(): ConversationConfiguration {
        if (mode == null) error("Conversation configuration is incomplete")
        return ConversationConfiguration(
            mode = mode.build(),
        )
    }
}

sealed interface ConversationModeBuilder {
    fun isValid(): Boolean
    fun build(): ConversationModeConfiguration
}

data class QuestionAnswerModeBuilder(
    val prompt: PromptCard? = null,
    val tools: List<ToolCard>? = null,
    val model: ModelCard? = null,
    val endMessageBuilder: (EphemeralMessageBuilder.() -> Unit)? = null,
) : ConversationModeBuilder {
    override fun isValid(): Boolean = model != null

    override fun build(): ConversationModeConfiguration {
        if (prompt == null || model == null) error("Conversation configuration is incomplete")

        return QuestionAnswerModeConfiguration(
            prompt = prompt,
            tools = tools ?: emptyList(),
            model = model,
        )
    }
}

data class RoleplayingModeBuilder(
    val character: Character? = null,
    val characterGreetingIndex: Int? = null,
    val userName: String? = null,
    val model: ModelCard? = null,
    val endMessageBuilder: (EphemeralMessageBuilder.() -> Unit)? = null,
) : ConversationModeBuilder {
    override fun isValid(): Boolean =
        model != null && character != null && userName != null

    override fun build(): ConversationModeConfiguration {
        if (character == null || userName == null || model == null)
            error("Conversation configuration is incomplete")

        return RoleplayingModeConfiguration(
            character = character,
            characterGreetingIndex = characterGreetingIndex,
            userName = userName,
            model = model,
        )
    }
}
