package io.github.ptitjes.konvo.frontend.discord

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.spi.*
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
    val tools: List<Tool> = emptyList(),
    val model: ModelCard? = null,
    val customSystemPrompt: String? = null,
    val endMessageBuilder: (EphemeralMessageBuilder.() -> Unit)? = null,
) : ConversationModeBuilder {
    override fun isValid(): Boolean = model != null

    override fun build(): ConversationModeConfiguration {
        if (model == null) error("Conversation configuration is incomplete")

        return QuestionAnswerModeConfiguration(
            tools = tools,
            model = model,
            customSystemPrompt = customSystemPrompt,
        )
    }
}

data class RoleplayingModeBuilder(
    val model: ModelCard? = null,
    val character: Character? = null,
    val characterGreetingIndex: Int? = null,
    val userName: String? = null,
    val endMessageBuilder: (EphemeralMessageBuilder.() -> Unit)? = null,
) : ConversationModeBuilder {
    override fun isValid(): Boolean =
        model != null && character != null && userName != null

    override fun build(): ConversationModeConfiguration {
        if (model == null || character == null || userName == null)
            error("Conversation configuration is incomplete")

        return RoleplayingModeConfiguration(
            model = model,
            character = character,
            characterGreetingIndex = characterGreetingIndex,
            userName = userName,
        )
    }
}
