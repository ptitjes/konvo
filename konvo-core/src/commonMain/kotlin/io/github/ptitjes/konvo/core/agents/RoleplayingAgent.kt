package io.github.ptitjes.konvo.core.agents

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.llms.*
import io.github.ptitjes.konvo.core.agents.toolkit.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.models.*
import kotlin.random.*

fun buildRoleplayAgent(
    model: ModelCard,
    character: CharacterCard,
    characterGreetingIndex: Int?,
    userName: String,
    roleplayAgentSettings: RoleplayAgentSettings,
): Agent {

    val systemPrompt = buildRoleplaySystemPrompt(
        defaultSystemPrompt = roleplayAgentSettings.defaultSystemPrompt
            .takeIf { it.isNotBlank() }
            ?: defaultSystemPrompt,
        character = character,
        userName = userName,
    )

    val greetings = character.greetings
    val greetingIndex = characterGreetingIndex ?: Random.nextInt(0, greetings.size)
    val initialAssistantMessage = greetings[greetingIndex].replaceTags(userName, character.name)

    val welcomeMessage = buildString {
        appendLine("![${character.name}](${character.avatarUrl})")
        appendLine()
        append(initialAssistantMessage)
    }

    return DefaultAgent(
        systemPrompt = prompt("role-play") { system { +systemPrompt } },
        welcomeMessage = welcomeMessage,
        model = model.toLLModel(),
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

private fun buildRoleplaySystemPrompt(
    defaultSystemPrompt: String,
    character: CharacterCard,
    userName: String,
    relevantPastConversationHistory: String? = null,
    characterMemory: String? = null,
): String = buildString {
    appendLine(character.systemPrompt ?: defaultSystemPrompt)
    onlyIf(character.description) { appendLine(it) }
    maybeAppendSection(character.personality, "\"{{char}}'s\" personality")
    maybeAppendSection(relevantPastConversationHistory, "Relevant past conversation history")
    maybeAppendSection(characterMemory, "\"{{char}}'s\" memories")
    maybeAppendSection(character.scenario, "The scenario of the conversation")
    maybeAppendSection(character.dialogueExamples, "How \"{{char}}\" speaks")
}.replaceTags(userName, character.name)

fun String.replaceTags(userName: String, characterName: String): String = this
    .replace("<user>", userName, true)
    .replace("{{user}}", userName, true)
    .replace("<bot>", characterName, true)
    .replace("{{char}}", characterName, true)

private val defaultSystemPrompt = """
    Write {{char}}'s next reply in a fictional chat between {{char}} and {{user}}.
""".trimIndent()

private fun StringBuilder.maybeAppendSection(variable: String?, label: String) {
    onlyIf(variable) {
        appendLine("# $label:")
        appendLine(it)
    }
}

private fun onlyIf(variable: String?, action: (String) -> Unit) {
    if (!variable.isNullOrBlank()) {
        action(variable)
    }
}
