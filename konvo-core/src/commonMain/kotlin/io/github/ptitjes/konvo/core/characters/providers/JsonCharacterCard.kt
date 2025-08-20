package io.github.ptitjes.konvo.core.characters.providers

import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.characters.ccv2.*
import io.github.ptitjes.konvo.core.characters.ccv3.*
import kotlinx.serialization.json.*

internal fun JsonObject.parseCharacterCard(id: String): CharacterCard {
    val spec = this["spec"]?.jsonPrimitive?.content

    return when (spec) {
        null -> {
            val data =
                Json.decodeFromJsonElement(DataV2.serializer(), this)
            parseCharacterCardV2(id, data)
        }

        "chara_card_v2" -> {
            val card = Json.decodeFromJsonElement(CharaCardV2.serializer(), this)
            parseCharacterCardV2(id, card.data)
        }

        "chara_card_v3" -> {
            val card = Json.decodeFromJsonElement(CharaCardV3.serializer(), this)
            parseCharacterCardV3(id, card.data)
        }

        else -> error("Unknown character format")
    }
}

private fun parseCharacterCardV2(
    id: String,
    data: DataV2,
): DefaultCharacterCard = DefaultCharacterCard(
    id = id,
    name = data.name,
    avatarUrl = data.avatar,
    description = data.description,
    personality = data.personality,
    scenario = data.scenario,
    dialogueExamples = data.messageExample,
    systemPrompt = data.systemPrompt?.takeIf { it.isNotBlank() },
    postHistoryInstructions = data.postHistoryInstructions?.takeIf { it.isNotBlank() },
    greetings = listOf(data.firstMessage) + data.alternateGreetings,
    tags = data.tags,
    characterBook = data.characterBook?.toLorebook(),
)

private fun parseCharacterCardV3(
    id: String,
    data: DataV3,
): DefaultCharacterCard {
    val avatarFromAssets =
        data.assets.firstOrNull { it.type.equals("icon", ignoreCase = true) && it.name == "main" }?.uri
            ?: data.assets.firstOrNull { it.type.equals("icon", ignoreCase = true) }?.uri

    return DefaultCharacterCard(
        id = id,
        name = data.name,
        avatarUrl = avatarFromAssets,
        description = data.description,
        personality = data.personality,
        scenario = data.scenario,
        dialogueExamples = data.messageExample,
        systemPrompt = data.systemPrompt?.takeIf { it.isNotBlank() },
        postHistoryInstructions = data.postHistoryInstructions?.takeIf { it.isNotBlank() },
        greetings = listOf(data.firstMessage) + data.alternateGreetings,
        tags = data.tags,
        characterBook = data.characterBook?.toLorebook(),
    )
}

private fun CharacterBookV2.toLorebook(): Lorebook = DefaultLorebook(
    name = name,
    description = description,
    scanDepth = scanDepth,
    tokenBudget = tokenBudget,
    recursiveScanning = recursiveScanning,
    entries = entries.map { it.toLorebookEntry() },
)

private fun CharacterBookEntryV2.toLorebookEntry(): LorebookEntry = DefaultLorebookEntry(
    enabled = enabled,
    constant = constant ?: false,
    keys = keys,
    useRegex = false,
    caseSensitive = caseSensitive ?: false,
    secondaryKeys = secondaryKeys,
    selective = selective,
    content = content,
    insertionOrder = insertionOrder,
    priority = priority,
    position = when (position) {
        EntryPositionV2.BeforeChar -> LorebookEntryPosition.BeforeChar
        EntryPositionV2.AfterChar -> LorebookEntryPosition.AfterChar
        EntryPositionV2.None, null -> null
    },
)

private fun CharacterBookV3.toLorebook(): Lorebook = DefaultLorebook(
    name = name,
    description = description,
    scanDepth = scanDepth,
    tokenBudget = tokenBudget,
    recursiveScanning = recursiveScanning,
    entries = entries.map { it.toLorebookEntry() },
)

private fun CharacterBookEntryV3.toLorebookEntry(): LorebookEntry = DefaultLorebookEntry(
    enabled = enabled,
    constant = constant ?: false,
    keys = keys,
    useRegex = useRegex,
    caseSensitive = caseSensitive ?: false,
    secondaryKeys = secondaryKeys,
    selective = selective,
    content = content,
    insertionOrder = insertionOrder,
    priority = priority,
    position = when (position) {
        EntryPositionV3.BeforeChar -> LorebookEntryPosition.BeforeChar
        EntryPositionV3.AfterChar -> LorebookEntryPosition.AfterChar
        EntryPositionV3.None, null -> null
    },
)
