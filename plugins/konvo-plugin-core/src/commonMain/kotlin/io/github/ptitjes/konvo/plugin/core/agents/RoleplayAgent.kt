package io.github.ptitjes.konvo.plugin.core.agents

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.llms.*
import ai.koog.prompt.message.*
import ai.koog.prompt.tokenizer.*
import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.plugin.core.agents.toolkit.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.events.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.files.*
import io.github.ptitjes.konvo.plugin.core.models.*
import io.github.ptitjes.konvo.plugin.core.roleplay.*
import io.github.ptitjes.konvo.plugin.core.settings.*
import kotlinx.coroutines.flow.*
import kotlin.random.*

class RoleplayAgent(
    private val modelProviderManager: ModelManager,
    private val characterProviderManager: CharacterManager,
    private val settingsRepository: SettingsRepository,
    private val lorebookManager: LorebookManager,
) : InteractiveAgent<RoleplayAgentConfiguration>(
    configurationClass = RoleplayAgentConfiguration::class,
    initialPrompt = { Prompt.Empty.copy(id = "roleplay") },
) {
    companion object {
        val agentId = "urn:$PLUGIN_ID/${RoleplayAgent::class.simpleName}"

        val presence = InteractionProtocol(
            id = "$agentId#Presence",
            awaitsInput = false,
            hidesParent = false,
            reactsTo = setOf(Messaging.Message::class)
        )

        val processing = InteractionProtocol(
            id = "$agentId#Processing",
            awaitsInput = false,
            hidesParent = false,
            reactsTo = setOf(AgentProcessing.Cancellation::class)
        )

        val protocols = setOf(presence, processing)

        init {
            protocols.forEach { InteractionProtocols.register(it) }
        }
    }

    override val initialInteraction by interaction<ConversationControl.InviteAgent, Unit>(presence) {
        onEnter {
            act(Presence.Joining)

            val character = characterProviderManager.withId(configuration.characterId)
            val personaSettings = settingsRepository.getSettings(PersonaSettingsKey).first()
            val persona = personaSettings.personas.first { it.name == configuration.personaName }

            val greetings = character.greetings
            val greetingIndex = configuration.characterGreetingIndex
                ?: Random.nextInt(0, greetings.size)
            val initialAssistantMessage = greetings[greetingIndex]
                .replaceTags(persona.nickname, character.name)

            val welcomeMessage = buildString {
                appendLine("![${character.name}](${character.avatarUrl})")
                appendLine()
                append(initialAssistantMessage)
            }

            act(welcomeMessage.toKonvoMessage())
            appendToPrompt { message(welcomeMessage.toKoogAssistantMessage()) }

            act(AgentCapabilities.Messaging())
        }
        onAction<Messaging.Message> { event ->
            runInteraction(processing, event)
        }
//      onEvent<AgentState.Update> { event ->
//          if (event.property == "configuration") {
//              model.update(modelManager.getModel(event.value))
//          }
//      }
        onLeave {
            act(Presence.Leaving)
        }
    }

    val processing by interaction<Messaging.Message, Unit>(RoleplayAgent.processing) {
        onEnter {
            act(AgentProcessing.Start)
        }
        onExecute { input ->
            val roleplaySettings = settingsRepository.getSettings(RoleplaySettingsKey).first()
            val model = modelProviderManager.named(configuration.modelName)
            val promptExecutor = SingleLLMPromptExecutor(model.getLLMClient())
            val character = characterProviderManager.withId(configuration.characterId)
            val personaSettings = settingsRepository.getSettings(PersonaSettingsKey).first()
            val persona = personaSettings.personas.first { it.name == configuration.personaName }
            val lorebook = configuration.lorebookId?.let { id -> lorebookManager.withId(id) }
            val developerSettings = settingsRepository.getSettings(DeveloperSettingsKey).first()

            val lorebooks: List<Lorebook> = buildList {
                character.characterBook?.let { add(it) }
                lorebook?.let { add(it) }
            }

            runGraph<Action<Messaging.Message>, Unit>(
                promptExecutor = promptExecutor,
                model = model.toLLModel(),
                maxAgentIterations = 50,
                input = input
            ) {
                val dumpRequest by dumpMessageAction()

                val request by executeRoleplayRequest(
                    roleplaySettings = roleplaySettings,
                    roleplayConfiguration = configuration,
                    character = character,
                    persona = persona,
                    lorebooks = lorebooks,
                )

                val emitResponses by actOnMessages()

                edge(nodeStart forwardTo dumpRequest)
                edge(dumpRequest forwardTo request)
                edge(request forwardTo emitResponses onMultipleAssistantMessages { true })
                edge(emitResponses forwardTo nodeFinish transformed { })
            }
        }
        onError {
            act(AgentProcessing.Failure(it.message ?: "Unknown error"))
        }
        onLeave {
            act(AgentProcessing.Completion)
        }
        onAction<AgentProcessing.Cancellation> {
            act(AgentProcessing.Failure("Cancelled by user"))
            leaveInteraction()
        }
    }
}

private val logger = KotlinLogging.logger { }

private fun AIAgentSubgraphBuilderBase<*, *>.executeRoleplayRequest(
    roleplaySettings: RoleplaySettings,
    roleplayConfiguration: RoleplayAgentConfiguration,
    character: CharacterCard,
    persona: Persona,
    lorebooks: List<Lorebook>,
) = node<Unit, List<Message.Response>>("roleplay-request") {
    llm.writeSession {
        if (!lorebooks.isEmpty()) {
            val selectedEntries = lorebooks.flatMap {
                it.selectEntries(
                    roleplaySettings = roleplaySettings,
                    roleplayConfiguration = roleplayConfiguration,
                    history = prompt.messages,
                )
            }

            logger.debug { "Selected ${selectedEntries.size} entries from ${lorebooks.size} lorebooks." }
            logger.trace { "Extracted content:\n${selectedEntries.joinToString("\n") { it.content }}" }

            val newSystemPrompt = buildRoleplaySystemPrompt(
                defaultSystemPrompt = roleplaySettings.defaultSystemPrompt
                    .takeIf { it.isNotBlank() }
                    ?: DEFAULT_ROLEPLAY_SYSTEM_PROMPT,
                character = character,
                userName = persona.nickname,
                lorebookEntries = selectedEntries,
            )

            prompt = prompt.replaceSystemPrompt(newSystemPrompt)
        }

        requestLLMMultiple().also {
            logger.debug { "Last token usage: ${prompt.latestTokenUsage}" }
        }
    }
}

private fun buildRoleplaySystemPrompt(
    defaultSystemPrompt: String,
    character: CharacterCard,
    userName: String,
    lorebookEntries: List<LorebookEntry>? = null,
    relevantPastConversationHistory: String? = null,
    characterMemory: String? = null,
): String = buildString {
    val (lorebookEntriesBefore, lorebookEntriesAfter) = lorebookEntries
        ?.asReversed()
        ?.partition { it.position == LorebookEntryPosition.BeforeChar }
        ?: (null to null)

    appendLine(character.systemPrompt ?: defaultSystemPrompt)
    onlyIf(character.description) { appendLine(it) }
    maybeAppendSection(lorebookEntriesBefore?.joinToString("\n") { it.content }, "Lorebook entries")
    maybeAppendSection(character.personality, "\"{{char}}'s\" personality")
    maybeAppendSection(relevantPastConversationHistory, "Relevant past conversation history")
    maybeAppendSection(characterMemory, "\"{{char}}'s\" memories")
    maybeAppendSection(lorebookEntriesAfter?.joinToString("\n") { it.content }, "Lorebook entries")
    maybeAppendSection(character.scenario, "The scenario of the conversation")
    maybeAppendSection(character.dialogueExamples, "How \"{{char}}\" speaks")
}.replaceTags(userName, character.name)

fun String.replaceTags(userName: String, characterName: String): String = this
    .replace("<user>", userName, true)
    .replace("{{user}}", userName, true)
    .replace("<bot>", characterName, true)
    .replace("{{char}}", characterName, true)

private val DEFAULT_ROLEPLAY_SYSTEM_PROMPT: String = """
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

private fun Prompt.replaceSystemPrompt(systemPrompt: String): Prompt {
    return prompt("role-play") {
        system { +systemPrompt }
        messages(messages.filter { it !is Message.System })
    }
}

/**
 * Selects the entries from the lorebook that are relevant to the conversation history.
 *
 * @param roleplaySettings The roleplay settings.
 * @param roleplayConfiguration The roleplay configuration.
 * @param history The conversation history.
 * @param tokenizer The tokenizer to use for token counting.
 * @return The relevant entries, ordered by relevance (from the most relevant to the least relevant).
 */
private fun Lorebook.selectEntries(
    roleplaySettings: RoleplaySettings,
    roleplayConfiguration: RoleplayAgentConfiguration,
    history: List<Message>,
    tokenizer: Tokenizer = SimpleRegexBasedTokenizer(),
): List<LorebookEntry> {
    // Determine effective parameters with sensible defaults
    val effectiveScanDepth = roleplayConfiguration.scanDepthOverride
        ?: this.scanDepth
        ?: roleplaySettings.defaultScanDepth
    val effectiveTokenBudget = roleplayConfiguration.tokenBudgetOverride
        ?: this.tokenBudget
        ?: roleplaySettings.defaultTokenBudget
    val effectiveRecursive = roleplayConfiguration.recursiveScanningOverride
        ?: this.recursiveScanning
        ?: roleplaySettings.defaultRecursiveScanning

    val historyMessages = history.filter { it is Message.User || it is Message.Assistant }
    // Build initial scan context from the last N messages
    val recentMessages = historyMessages.takeLast(effectiveScanDepth)
    // We do not rely on Message internals; toString() is acceptable as a fallback representation
    val baseContext = recentMessages.joinToString("\n") { it.toString() }

    // Filter out entries that are disabled or empty content
    val candidateEntries = entries.filter { it.enabled && it.content.isNotBlank() }

    // Helper: string/regex matching according to entry settings
    fun matchesInText(entry: LorebookEntry, text: String): Boolean {
        // If entry is marked constant, include it regardless of keys/secondary keys,
        // unless use_regex is true (per CCv3 spec note to ignore constant with regex).
        if (entry.constant && !entry.useRegex) return true

        val caseSensitive = entry.caseSensitive
        return if (entry.useRegex) {
            // Per spec, applications MAY use only the first regex for performance
            val patternRaw = entry.keys.firstOrNull() ?: return false
            try {
                val regex = if (caseSensitive) Regex(patternRaw) else Regex(patternRaw, RegexOption.IGNORE_CASE)
                regex.containsMatchIn(text)
            } catch (_: Throwable) {
                // Invalid regex -> not a match
                false
            }
        } else {
            val haystack = if (caseSensitive) text else text.lowercase()
            val primaryMatch = entry.keys.any { key ->
                val needle = if (caseSensitive) key else key.lowercase()
                haystack.contains(needle)
            }
            if (!primaryMatch) return false

            // selective + secondary_keys rule
            val selective = entry.selective == true
            val secondaryKeys = entry.secondaryKeys
            if (selective && !secondaryKeys.isNullOrEmpty()) {
                val secondaryMatch = secondaryKeys.any { key ->
                    val sNeedle = if (caseSensitive) key else key.lowercase()
                    haystack.contains(sNeedle)
                }
                if (!secondaryMatch) return false
            }
            true
        }
    }

    // Recursive scanning: iteratively expand context with matched entries' contents
    val selected = LinkedHashSet<LorebookEntry>()

    var context = baseContext

    if (effectiveRecursive) {
        var changed: Boolean
        do {
            changed = false
            for (entry in candidateEntries) {
                if (entry in selected) continue
                if (matchesInText(entry, context)) {
                    selected.add(entry)
                    // expand the context with this entry's content for subsequent matches
                    context += "\n" + entry.content
                    changed = true
                }
            }
        } while (changed)
    } else {
        for (entry in candidateEntries) {
            if (matchesInText(entry, context)) {
                selected.add(entry)
            }
        }
    }

    // Sort entries by relevance: higher priority first (nulls last), then lower insertionOrder first
    val ordered = selected.toList().sortedWith(
        compareByDescending<LorebookEntry> { it.priority ?: Int.MIN_VALUE }
            .thenBy { it.insertionOrder }
    )

    var result = ordered
    val totalTokens = result.sumOf { tokenizer.countTokens(it.content) }
    if (effectiveTokenBudget in 1..<totalTokens) {
        val mutable = result.toMutableList()
        var tokens = totalTokens
        var index = mutable.lastIndex
        while (index >= 0 && tokens > effectiveTokenBudget) {
            tokens -= tokenizer.countTokens(mutable[index].content)
            mutable.removeAt(index)
            index -= 1
        }
        result = mutable
    }

    return result
}
