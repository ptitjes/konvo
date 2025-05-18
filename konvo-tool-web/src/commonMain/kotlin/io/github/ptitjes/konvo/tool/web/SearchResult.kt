package io.github.ptitjes.konvo.tool.web

import kotlinx.serialization.*

@Serializable
data class SearchResult(
    val title: String,
    val url: String,
    val snippet: String,
)
