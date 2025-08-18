package io.github.ptitjes.konvo.core.characters.providers

import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.characters.ccv2.*
import kotlinx.serialization.json.*

internal fun JsonObject.parseCharacterCard(id: String): CharacterCard {
    val spec = this["spec"]?.jsonPrimitive?.content

    val data: Data = when (spec) {
        null -> Json.decodeFromJsonElement(Data.serializer(), this)
        "chara_card_v2" -> {
            val card = Json.decodeFromJsonElement(CharaCardV2.serializer(), this)
            card.data
        }

        else -> error("Unknown character format")
    }

    return DefaultCharacterCard(
        id = id,
        name = data.name,
        avatarUrl = data.avatar,
        description = data.description,
        personality = data.personality,
        scenario = data.scenario,
        messageExample = data.messageExample,
        greetings = listOf(data.firstMessage) + data.alternateGreetings,
        tags = data.tags,
    )
}

