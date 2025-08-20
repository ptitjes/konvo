package io.github.ptitjes.konvo.frontend.discord

import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.prompts.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

data class ConversationBuilder(
    val mode: ConversationModeBuilder? = null,
    val endMessageBuilder: (EphemeralMessageBuilder.() -> Unit)? = null,
) {
    fun isValid(): Boolean = mode != null && mode.isValid()

    fun build(): ConversationConfiguration {
        if (mode == null) error("Conversation configuration is incomplete")
        return ConversationConfiguration(
            agent = mode.build(),
        )
    }
}

sealed interface ConversationModeBuilder {
    fun isValid(): Boolean
    fun build(): AgentConfiguration
}

data class QuestionAnswerModeBuilder(
    val prompt: PromptCard? = null,
    val mcpServerNames: Set<String>? = null,
    val model: ModelCard? = null,
    val endMessageBuilder: (EphemeralMessageBuilder.() -> Unit)? = null,
) : ConversationModeBuilder {
    override fun isValid(): Boolean = prompt != null && model != null

    override fun build(): AgentConfiguration {
        if (prompt == null || model == null) error("Conversation configuration is incomplete")

        return QuestionAnswerAgentConfiguration(
            mcpServerNames = mcpServerNames ?: emptySet(),
            modelName = model.name,
        )
    }
}

data class RoleplayModeBuilder(
    val character: CharacterCard? = null,
    val characterGreetingIndex: Int? = null,
    val userName: String? = null,
    val model: ModelCard? = null,
    val endMessageBuilder: (EphemeralMessageBuilder.() -> Unit)? = null,
) : ConversationModeBuilder {
    override fun isValid(): Boolean =
        model != null && character != null && userName != null

    override fun build(): AgentConfiguration {
        if (character == null || userName == null || model == null)
            error("Conversation configuration is incomplete")

        return RoleplayAgentConfiguration(
            characterId = character.id,
            characterGreetingIndex = characterGreetingIndex,
            userName = userName,
            modelName = model.name,
        )
    }
}
