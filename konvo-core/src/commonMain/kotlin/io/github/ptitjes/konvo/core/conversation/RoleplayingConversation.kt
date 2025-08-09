package io.github.ptitjes.konvo.core.conversation

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.llms.*
import io.github.ptitjes.konvo.core.ai.koog.*
import kotlin.random.*

fun buildRoleplayingAgent(configuration: RoleplayingAgentConfiguration): ChatAgent {
    val model = configuration.model

    val character = configuration.character
    val userName = configuration.userName

    val greetings = character.greetings
    val greetingIndex = configuration.characterGreetingIndex ?: Random.nextInt(0, greetings.size)
    val initialAssistantMessage = greetings[greetingIndex].replaceTags(userName, character.name)

    val welcomeMessage = "![${character.name}](${character.avatarUrl})\n\n$initialAssistantMessage"

    return ChatAgent(
        systemPrompt = prompt("roleplaying") {
            system { +character.systemPrompt.replaceTags(userName, character.name) }
        },
        initialAssistantMessage = welcomeMessage,
        model = model.toLLModel(),
        maxAgentIterations = 50,
        promptExecutor = SingleLLMPromptExecutor(model.getLLMClient()),
        strategy = {
            strategy("roleplaying") {
                val dumpRequest by dumpToPrompt()
                val request by requestLLM()

                edge(nodeStart forwardTo dumpRequest)
                edge(dumpRequest forwardTo request)
                edge(request forwardTo nodeFinish onMultipleAssistantMessages { true })
            }
        },
    )
}

fun String.replaceTags(userName: String, characterName: String): String {
    return this.replace("<user>", userName, true).replace("{{user}}", userName, true)
        .replace("<bot>", characterName, true).replace("{{char}}", characterName, true)
}
