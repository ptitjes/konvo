package io.github.ptitjes.konvo.core.conversations.model

import kotlinx.serialization.json.*

class ToolCall(
    val id: String,
    val tool: String,
    val arguments: Map<String, JsonElement>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other::class != this::class) return false
        other as ToolCall
        return id == other.id
    }

    override fun hashCode(): Int = id.hashCode()

    override fun toString(): String =
        "VetoableToolCall(id=$id, tool=$tool, arguments=$arguments)"
}
