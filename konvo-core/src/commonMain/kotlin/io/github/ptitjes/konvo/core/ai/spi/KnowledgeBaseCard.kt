package io.github.ptitjes.konvo.core.ai.spi

import ai.koog.rag.base.*
import kotlinx.io.files.*

interface KnowledgeBaseCard {
    val provider: KnowledgeBaseProvider
    val name: String

    fun getRankedDocumentStorage(): RankedDocumentStorage<MarkdownFragment>
}

data class MarkdownFragment(
    val path: Path,
    val content: String,
    val startIndex: Int,
    val endIndex: Int,
)
