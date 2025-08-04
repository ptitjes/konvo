package io.github.ptitjes.konvo.core.conversation

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.llms.*
import io.github.ptitjes.konvo.core.ai.koog.*
import kotlinx.coroutines.*
import kotlin.random.*

class RoleplayingConversation(
    coroutineScope: CoroutineScope,
    override val configuration: RoleplayingModeConfiguration,
) : Conversation(coroutineScope) {
    override suspend fun buildChatAgent(): ChatAgent {
        val model = configuration.model

        return ChatAgent(
            initialPrompt = prompt("roleplaying") {
                system { +buildSystemPrompt() }
                getInitialAssistantMessage().let { assistant { +it } }
            },
            model = model.toLLModel(),
            maxAgentIterations = 50,
            promptExecutor = SingleLLMPromptExecutor(model.getLLMClient()),
            strategy = strategy("roleplaying") {
                val dumpRequest by dumpToPrompt()
                val request by requestLLM()

                edge(nodeStart forwardTo dumpRequest)
                edge(dumpRequest forwardTo request)
                edge(request forwardTo nodeFinish onMultipleAssistantMessages { true })
            },
        )
    }

    private val character = configuration.character
    private val userName = configuration.userName

    fun buildSystemPrompt(): String {
        return character.systemPrompt.replaceTags(userName, character.name)
    }

    override fun getInitialAssistantMessage(): String {
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
