package io.github.ptitjes.konvo.core.roleplay.providers

import io.github.ptitjes.konvo.core.roleplay.*

internal data class DefaultLorebook(
    override val id: String?,
    override val name: String?,
    override val description: String?,
    override val scanDepth: Int?,
    override val tokenBudget: Int?,
    override val recursiveScanning: Boolean?,
    override val entries: List<LorebookEntry>,
) : Lorebook

internal data class DefaultLorebookEntry(
    override val enabled: Boolean,
    override val constant: Boolean,
    override val keys: List<String>,
    override val useRegex: Boolean,
    override val caseSensitive: Boolean,
    override val secondaryKeys: List<String>?,
    override val selective: Boolean?,
    override val content: String,
    override val insertionOrder: Int,
    override val priority: Int?,
    override val position: LorebookEntryPosition?,
) : LorebookEntry
