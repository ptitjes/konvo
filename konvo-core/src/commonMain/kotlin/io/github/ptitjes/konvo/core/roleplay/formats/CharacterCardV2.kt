package io.github.ptitjes.konvo.core.roleplay.formats

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * Serializable types to parse the Character Card V2 format as defined in docs/characters/spec_v2.md
 */
@Serializable
data class CharaCardV2(
    /** Must be "chara_card_v2" */
    @SerialName("spec") val spec: String = "chara_card_v2",
    /** Must be "2.0" */
    @SerialName("spec_version") val specVersion: String = "2.0",
    val data: DataV2,
)

@Serializable
data class DataV2(
    val name: String,
    val description: String,
    val personality: String,
    val scenario: String,
    @SerialName("first_mes") val firstMessage: String,
    @SerialName("mes_example") val messageExample: String,

    // New fields
    @SerialName("creator_notes") val creatorNotes: String? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("post_history_instructions") val postHistoryInstructions: String? = null,
    @SerialName("alternate_greetings") val alternateGreetings: List<String> = emptyList(),
    @SerialName("character_book") val characterBook: CharacterBookV2? = null,

    // May 8th additions
    val tags: List<String> = emptyList(),
    val creator: String? = null,
    @SerialName("character_version") val characterVersion: String? = null,
    val extensions: JsonObject = buildJsonObject { },

    // Some sources add an avatar field even if not in the spec; tolerate it to be flexible
    val avatar: String? = null,
)

@Serializable
data class CharacterBookV2(
    val name: String? = null,
    val description: String? = null,
    @SerialName("scan_depth") val scanDepth: Int? = null,
    @SerialName("token_budget") val tokenBudget: Int? = null,
    @SerialName("recursive_scanning") val recursiveScanning: Boolean? = null,
    val extensions: JsonObject = buildJsonObject { },
    val entries: List<CharacterBookEntryV2> = emptyList(),
)

@Serializable
data class CharacterBookEntryV2(
    val keys: List<String>,
    val content: String,
    val extensions: JsonObject = buildJsonObject { },
    val enabled: Boolean = true,
    @SerialName("insertion_order") val insertionOrder: Int,
    @SerialName("case_sensitive") val caseSensitive: Boolean? = null,

    // No current equivalent in Silly Tavern
    val name: String? = null,
    val priority: Int? = null,

    // No current equivalent in Agnai
    val id: Int? = null,
    val comment: String? = null,
    val selective: Boolean? = null,
    @SerialName("secondary_keys") val secondaryKeys: List<String>? = null,
    val constant: Boolean? = null,
    val position: EntryPositionV2? = null,

    // Unknown provenance
    val probability: Int? = null,
    val selectiveLogic: Int? = null,
)

@Serializable
enum class EntryPositionV2 {
    @SerialName("before_char")
    BeforeChar,

    @SerialName("after_char")
    AfterChar,

    @SerialName("")
    None,
}
