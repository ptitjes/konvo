package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import com.mikepenz.markdown.coil3.*
import com.mikepenz.markdown.compose.components.*
import com.mikepenz.markdown.compose.elements.*
import com.mikepenz.markdown.m3.*
import com.mikepenz.markdown.model.*
import dev.snipme.highlights.*
import dev.snipme.highlights.model.*
import io.github.ptitjes.konvo.frontend.compose.theme.*
import com.mikepenz.markdown.model.State as MarkdownViewState

@Composable
fun MarkdownContent(
    state: MarkdownViewState,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    val isDark = LocalTheme.current.isDark
    val highlightsBuilder = remember(isDark) {
        Highlights.Builder().theme(SyntaxThemes.atom(darkMode = isDark))
    }

    Markdown(
        state = state,
        colors = markdownColor(
            text = textColor,
        ),
        animations = markdownAnimations(
            animateTextSize = { this }
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

