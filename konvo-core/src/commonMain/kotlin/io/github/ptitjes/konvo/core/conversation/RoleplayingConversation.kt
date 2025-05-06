package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.*
import kotlinx.coroutines.*
import kotlin.random.*

class RoleplayingConversation(
    coroutineScope: CoroutineScope,
    override val configuration: RoleplayingModeConfiguration,
) : TurnBasedConversation(coroutineScope) {
    override fun buildModel() = buildModel(configuration.model)

    private val character = configuration.character
    private val userName = configuration.userName

    override fun buildSystemPrompt(): String {
        return character.systemPrompt.replaceTags(userName, character.name)
    }

    override val hasInitialAssistantMessage: Boolean get() = true

    override fun buildInitialAssistantMessage(): String? {
        val greetings = character.greetings
        val greetingIndex = configuration.characterGreetingIndex ?: Random.Default.nextInt(0, greetings.size)
        val selectedGreeting = greetings[greetingIndex]

        return selectedGreeting.replaceTags(userName, character.name)
    }
}

fun String.replaceTags(userName: String, characterName: String): String {
    return this.replace("<user>", userName, true).replace("{{user}}", userName, true)
        .replace("<bot>", characterName, true).replace("{{char}}", characterName, true)
}
