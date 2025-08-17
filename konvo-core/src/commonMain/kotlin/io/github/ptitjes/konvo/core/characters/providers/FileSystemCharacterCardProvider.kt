package io.github.ptitjes.konvo.core.characters.providers

import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.base.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.io.files.*
import kotlinx.serialization.json.*

class FileSystemCharacterCardProvider(
    storagePaths: StoragePaths,
) : CharacterCardProvider {
    override val name: String? = null

    private val path = Path(storagePaths.dataDirectory, "characters")

    override suspend fun query(): List<CharacterCard> {
        return defaultFileSystem
            .loadFiles(path, "json") { path ->
                val json = Json.decodeFromString<JsonObject>(readText(path))
                json.parseCharacterCard()
            }
            .sortedBy { it.name }
    }
}

private fun JsonObject.parseCharacterCard(): CharacterCard {
    val spec = this["spec"]?.jsonPrimitive?.content

    if (spec != null && spec != "chara_card_v2") error("Unknown character format")

    val data = if (spec == null) this else this["data"]!!.jsonObject

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

    return DefaultCharacterCard(
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

private data class DefaultCharacterCard(
    override val name: String,
    override val avatarUrl: String? = null,
    override val description: String,
    override val personality: String,
    override val scenario: String,
    override val messageExample: String,
    override val greetings: List<String>,
    override val tags: List<String>,
) : CharacterCard
