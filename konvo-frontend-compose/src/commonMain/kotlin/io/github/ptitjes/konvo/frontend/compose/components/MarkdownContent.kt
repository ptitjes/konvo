package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import com.mikepenz.markdown.coil3.*
import com.mikepenz.markdown.compose.components.*
import com.mikepenz.markdown.compose.elements.*
import com.mikepenz.markdown.m3.*
import dev.snipme.highlights.*
import dev.snipme.highlights.model.*
import io.github.ptitjes.konvo.frontend.compose.theme.*

@Composable
fun MarkdownContent(
    content: String,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalTheme.current.isDark
    val highlightsBuilder = remember(isDark) {
        Highlights.Builder().theme(SyntaxThemes.atom(darkMode = isDark))
    }

    Markdown(
        content = content,
        colors = markdownColor(
            text = textColor,
        ),
        imageTransformer = Coil3ImageTransformerImpl,
        components = markdownComponents(
            codeBlock = {
                MarkdownHighlightedCodeBlock(
                    content = it.content,
                    node = it.node,
                    highlights = highlightsBuilder,
                )
            },
            codeFence = {
                MarkdownHighlightedCodeFence(
                    content = it.content,
                    node = it.node,
                    highlights = highlightsBuilder,
                )
            },
        ),
        modifier = modifier,
    )
}
