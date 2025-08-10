package io.github.ptitjes.konvo.core.ai.spi

import io.github.ptitjes.konvo.core.*
import kotlinx.io.files.*
import kotlinx.serialization.json.*

data class CharacterCard(
    val name: String,
    val avatarUrl: String? = null,
    val description: String,
    val personality: String,
    val scenario: String,
    val messageExample: String,
    val greetings: List<String>,
    val tags: List<String>,
) {
    val systemPrompt: String
        get() = buildString {
            append(description)
            append("\n")
            append("Here is {{char}}'s personality: $personality\n")
            append("Here is the scenario: $scenario\n")
            append("Here are an example conversation: $messageExample\n")
        }

    companion object
}

fun CharacterCard.Companion.loadCharacters(charactersDirectory: Path): List<CharacterCard> {
    return defaultFileSystem.list(charactersDirectory).mapNotNull { characterPath ->
        runCatching { loadCharacter(characterPath) }.getOrNull()
    }.sortedBy { it.name }
}

fun CharacterCard.Companion.loadCharacter(path: Path): CharacterCard = defaultFileSystem.readJsonCharacter(path)

private fun FileSystem.readJsonCharacter(path: Path): CharacterCard {
    return CharacterCard.fromJson(readJson(path))
}

fun CharacterCard.Companion.fromJson(json: JsonObject): CharacterCard {
    val spec = json["spec"]?.jsonPrimitive?.content

    if (spec != null && spec != "chara_card_v2") error("Unknown character format")

    val data = if (spec == null) json else json["data"]!!.jsonObject

    val name = data["name"]!!.jsonPrimitive.content
    val avatar = data["avatar"]?.jsonPrimitive?.content
    val description = data["description"]!!.jsonPrimitive.content
    val personality = data["personality"]!!.jsonPrimitive.content
    val scenario = data["scenario"]!!.jsonPrimitive.content
    val messageExample = data["mes_example"]!!.jsonPrimitive.content
    val firstMessage = data["first_mes"]!!.jsonPrimitive.content
    val tags = data["tags"]?.jsonArray?.map { it.jsonPrimitive.content } ?: listOf()

    val alternateGreetings =
        if (spec == null) listOf()
        else (data["alternate_greetings"]!!.jsonArray.map { it.jsonPrimitive.content })

    return CharacterCard(
        name = name,
        avatarUrl = avatar,
        description = description,
        personality = personality,
        scenario = scenario,
        messageExample = messageExample,
        greetings = listOf(firstMessage) + alternateGreetings,
        tags = tags,
    )
}
