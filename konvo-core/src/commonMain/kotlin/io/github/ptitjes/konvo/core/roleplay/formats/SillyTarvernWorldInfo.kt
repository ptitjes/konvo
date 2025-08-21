package io.github.ptitjes.konvo.core.roleplay.formats

import kotlinx.serialization.*
import kotlinx.serialization.json.*

@Serializable
data class SillyTavernWorldInfo(
    val name: String? = null,
    val description: String? = null,
    @SerialName("is_creation") val isCreation: Boolean? = false,
    @SerialName("scan_depth") val scanDepth: Int? = null,
    @SerialName("token_budget") val tokenBudget: Int? = null,
    @SerialName("recursive_scanning") val recursiveScanning: Boolean? = null,
    val extensions: JsonObject = buildJsonObject { },
    val entries: Map<String, SillyTavernWorldInfoEntry> = emptyMap(),
)

@Serializable
data class SillyTavernWorldInfoEntry(
    val uid: Int,
    @SerialName("key") val keys: List<String>,
    @SerialName("keysecondary") val secondaryKeys: List<String>? = null,
    val comment: String? = null,
    val content: String,
    val constant: Boolean? = null,
    val selective: Boolean? = null,
    val order: Int? = null,
    val position: Int? = null,
    val disable: Boolean? = null,
    val caseSensitive: Boolean? = null,
)
