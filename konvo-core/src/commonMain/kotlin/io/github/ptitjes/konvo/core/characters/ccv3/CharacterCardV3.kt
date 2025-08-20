package io.github.ptitjes.konvo.core.characters.ccv3

import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * Serializable types to parse the Character Card V3 format as defined in docs/characters/spec_v3.md
 */
@Serializable
data class CharaCardV3(
    /** Must be "chara_card_v3" */
    @SerialName("spec") val spec: String = "chara_card_v3",
    /** Should be "3.0" but may vary in future */
    @SerialName("spec_version") val specVersion: String = "3.0",
    val data: DataV3,
)

@Serializable
data class DataV3(
    // Fields from CCv2
    val name: String,
    val description: String,
    val personality: String,
    val scenario: String,
    @SerialName("first_mes") val firstMessage: String,
    @SerialName("mes_example") val messageExample: String,

    // CCv2 optional/additional fields still present
    @SerialName("creator_notes") val creatorNotes: String? = null,
    @SerialName("system_prompt") val systemPrompt: String? = null,
    @SerialName("post_history_instructions") val postHistoryInstructions: String? = null,
    @SerialName("alternate_greetings") val alternateGreetings: List<String> = emptyList(),
    @SerialName("character_book") val characterBook: CharacterBookV3? = null,

    // May 8th additions (v2) also present
    val tags: List<String> = emptyList(),
    val creator: String? = null,
    @SerialName("character_version") val characterVersion: String? = null,
    val extensions: JsonObject = buildJsonObject { },

    // CCv3 new fields
    val assets: List<AssetV3> = emptyList(),
    val nickname: String? = null,
    @SerialName("creator_notes_multilingual") val creatorNotesMultilingual: Map<String, String>? = null,
    val source: List<String>? = null,
    @SerialName("group_only_greetings") val groupOnlyGreetings: List<String> = emptyList(),
    @SerialName("creation_date") val creationDate: Long? = null,
    @SerialName("modification_date") val modificationDate: Long? = null,
)

@Serializable
data class AssetV3(
    val type: String,
    val uri: String,
    val name: String,
    val ext: String,
)

@Serializable
data class CharacterBookV3(
    val name: String? = null,
    val description: String? = null,
    @SerialName("scan_depth") val scanDepth: Int? = null,
    @SerialName("token_budget") val tokenBudget: Int? = null,
    @SerialName("recursive_scanning") val recursiveScanning: Boolean? = null,
    val extensions: JsonObject = buildJsonObject { },
    val entries: List<CharacterBookEntryV3> = emptyList(),
)

@Serializable
data class CharacterBookEntryV3(
    val keys: List<String>,
    val content: String,
    val extensions: JsonObject = buildJsonObject { },
    val enabled: Boolean = true,
    @SerialName("insertion_order") val insertionOrder: Int,
    @SerialName("case_sensitive") val caseSensitive: Boolean? = null,

    // V3 additions
    @SerialName("use_regex") val useRegex: Boolean = false,

    // Optional or compatibility fields
    val name: String? = null,
    val priority: Int? = null,
    val id: JsonElement? = null, // number or string
    val comment: String? = null,
    val selective: Boolean? = null,
    @SerialName("secondary_keys") val secondaryKeys: List<String>? = null,
    val constant: Boolean? = null,
    val position: EntryPositionV3? = null,
)

@Serializable
enum class EntryPositionV3 {
    @SerialName("before_char")
    BeforeChar,

    @SerialName("after_char")
    AfterChar,

    @SerialName("")
    None,
}
