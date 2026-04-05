package io.github.ptitjes.konvo.plugin.roleplay.formats

import io.github.ptitjes.konvo.plugin.roleplay.*
import kotlinx.serialization.json.*

private val lenientJson = Json { ignoreUnknownKeys = true }

internal fun String.parseLorebook(id: String): Lorebook {
    val json = lenientJson.decodeFromString<JsonObject>(this)
    val spec = json["spec"]?.jsonPrimitive?.content
    val entries = json["entries"]

    return when (spec) {
        null if (entries != null && entries is JsonObject) -> {
            val data =
                lenientJson.decodeFromJsonElement(SillyTavernWorldInfo.serializer(), json)
            data.toLorebook(id)
        }

        null if (entries != null && entries is JsonArray) -> {
            val data =
                lenientJson.decodeFromJsonElement(CharacterBookV2.serializer(), json)
            data.toLorebook(id)
        }

        "lorebook_v3" -> {
            val card = lenientJson.decodeFromJsonElement(LorebookV3.serializer(), json)
            card.data.toLorebook(id)
        }

        else -> error("Unknown lorebook format")
    }
}

internal fun SillyTavernWorldInfo.toLorebook(id: String?): Lorebook = DefaultLorebook(
    id = id,
    name = name,
    description = description,
    scanDepth = scanDepth,
    tokenBudget = tokenBudget,
    recursiveScanning = recursiveScanning,
    entries = entries.map { (_, entry) -> entry.toLorebookEntry() },
)

internal fun SillyTavernWorldInfoEntry.toLorebookEntry(): LorebookEntry = DefaultLorebookEntry(
    enabled = !(disable ?: false),
    constant = constant ?: false,
    keys = keys,
    useRegex = false,
    caseSensitive = caseSensitive ?: false,
    secondaryKeys = secondaryKeys ?: emptyList(),
    selective = selective ?: false,
    content = content,
    insertionOrder = order ?: 0,
    priority = null,
    position = LorebookEntryPosition.AfterChar
)

internal fun CharacterBookV2.toLorebook(id: String?): Lorebook = DefaultLorebook(
    id = id,
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

internal fun CharacterBookV3.toLorebook(id: String?): Lorebook = DefaultLorebook(
    id = id,
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
