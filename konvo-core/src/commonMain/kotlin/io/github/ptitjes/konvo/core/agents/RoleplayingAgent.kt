package io.github.ptitjes.konvo.core.agents

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.llms.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.models.*
import kotlin.random.*

fun buildRoleplayAgent(
    model: Model,
    character: CharacterCard,
    characterGreetingIndex: Int?,
    userName: String,
): Agent {
    val greetings = character.greetings
    val greetingIndex = characterGreetingIndex ?: Random.nextInt(0, greetings.size)
    val initialAssistantMessage = greetings[greetingIndex].replaceTags(userName, character.name)

    val welcomeMessage = "![${character.name}](${character.avatarUrl})\n\n$initialAssistantMessage"

    return ChatAgent(
        systemPrompt = prompt("role-play") {
            system { +character.systemPrompt.replaceTags(userName, character.name) }
        },
        welcomeMessage = welcomeMessage,
        model = model.toLLModel(),
        maxAgentIterations = 50,
        promptExecutor = SingleLLMPromptExecutor(model.getLLMClient()),
        strategy = {
            strategy("role-play") {
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
