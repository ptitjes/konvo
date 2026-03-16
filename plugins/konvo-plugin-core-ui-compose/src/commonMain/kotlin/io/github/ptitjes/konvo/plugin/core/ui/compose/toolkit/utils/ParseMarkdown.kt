package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.utils

import com.mikepenz.markdown.model.*
import kotlinx.coroutines.flow.*

suspend fun parseMarkdown(content: String): State =
    parseMarkdownFlow(content).first { it is State.Success || it is State.Error }
