package io.github.ptitjes.konvo.core.roleplay

import kotlinx.serialization.*

interface Lorebook {
    val id: String?
    val name: String?
    val description: String?
    val scanDepth: Int?
    val tokenBudget: Int?
    val recursiveScanning: Boolean?
    val entries: List<LorebookEntry>
}

interface LorebookEntry {
    val enabled: Boolean
    val constant: Boolean

    val keys: List<String>
    val useRegex: Boolean
    val caseSensitive: Boolean

    val secondaryKeys: List<String>?
    val selective: Boolean?

    val content: String

    val insertionOrder: Int

    val priority: Int?
    val position: LorebookEntryPosition?
}

@Serializable
enum class LorebookEntryPosition {
    @SerialName("before_char")
    BeforeChar,

    @SerialName("after_char")
    AfterChar,
}
