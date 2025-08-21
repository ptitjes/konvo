package io.github.ptitjes.konvo.core.agents

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.llms.*
import ai.koog.prompt.message.*
import ai.koog.prompt.tokenizer.*
import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.agents.toolkit.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.roleplay.*
import kotlin.random.*

private val logger = KotlinLogging.logger { }

fun buildRoleplayAgent(
    roleplaySettings: RoleplaySettings,
    roleplayConfiguration: RoleplayAgentConfiguration,
    model: ModelCard,
    character: CharacterCard,
    lorebook: Lorebook?,
): Agent {
    val characterGreetingIndex = roleplayConfiguration.characterGreetingIndex
    val userName = roleplayConfiguration.userName

    val initialSystemPrompt = buildRoleplaySystemPrompt(
        defaultSystemPrompt = roleplaySettings.defaultSystemPrompt
            .takeIf { it.isNotBlank() }
            ?: DEFAULT_SYSTEM_PROMPT,
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

    val lorebooks: List<Lorebook> = buildList {
        character.characterBook?.let { add(it) }
        lorebook?.let { add(it) }
    }

    return DefaultAgent(
        systemPrompt = prompt("role-play") { system { +initialSystemPrompt } },
        welcomeMessage = welcomeMessage,
        model = model.toLLModel(),
        promptExecutor = SingleLLMPromptExecutor(model.getLLMClient()),
        strategy = {
            strategy("role-play") {
                val dumpRequest by dumpToPrompt()

                val request by executeRoleplayRequest(
                    roleplaySettings = roleplaySettings,
                    roleplayConfiguration = roleplayConfiguration,
                    character = character,
                    lorebooks = lorebooks,
                )

                edge(nodeStart forwardTo dumpRequest)
                edge(dumpRequest forwardTo request)
                edge(request forwardTo nodeFinish onMultipleAssistantMessages { true })
            }
        },
    )
}

private fun AIAgentStrategyBuilder<Message.User, List<Message.Assistant>>.executeRoleplayRequest(
    roleplaySettings: RoleplaySettings,
    roleplayConfiguration: RoleplayAgentConfiguration,
    character: CharacterCard,
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

            logger.debug {
                "Selected ${selectedEntries.size} entries from ${lorebooks.size} lorebooks:\n${selectedEntries.joinToString("\n")}\n"
            }

            val newSystemPrompt = buildRoleplaySystemPrompt(
                defaultSystemPrompt = roleplaySettings.defaultSystemPrompt
                    .takeIf { it.isNotBlank() }
                    ?: DEFAULT_ROLEPLAY_SYSTEM_PROMPT,
                character = character,
                userName = roleplayConfiguration.userName,
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
 * @param history The conversation history.
 * @param scanDepth The maximum depth to scan the lorebook.
 * @param tokenBudget The maximum number of tokens to use.
 * @param recursiveScanning Whether to recursively scan the lorebook.
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
            return try {
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
