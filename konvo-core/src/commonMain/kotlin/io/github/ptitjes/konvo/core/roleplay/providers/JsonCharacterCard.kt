package io.github.ptitjes.konvo.core.roleplay.providers

import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.roleplay.formats.*
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
    characterBook = data.characterBook?.toLorebook(null),
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
        characterBook = data.characterBook?.toLorebook(null),
    )
}
