package io.github.ptitjes.konvo.frontend.compose.toolkit.widgets

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.painter.*
import coil3.compose.*
import coil3.request.*
import com.mikepenz.markdown.compose.components.*
import com.mikepenz.markdown.compose.elements.*
import com.mikepenz.markdown.m3.*
import com.mikepenz.markdown.model.*
import dev.snipme.highlights.*
import dev.snipme.highlights.model.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.theme.*
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
        typography = markdownTypography(
            h1 = MaterialTheme.typography.headlineLarge,
            h2 = MaterialTheme.typography.headlineMedium,
            h3 = MaterialTheme.typography.headlineSmall,
            h4 = MaterialTheme.typography.titleLarge,
            h5 = MaterialTheme.typography.titleMedium,
            h6 = MaterialTheme.typography.titleSmall,
        ),
        animations = markdownAnimations(
            animateTextSize = { this }
        ),
        imageTransformer = CustomCoil3ImageTransformer,
        components = markdownComponents(
            codeBlock = {
                MarkdownHighlightedCodeBlock(
                    content = it.content,
                    node = it.node,
                    highlightsBuilder = highlightsBuilder,
                )
            },
            codeFence = {
                MarkdownHighlightedCodeFence(
                    content = it.content,
                    node = it.node,
                    highlightsBuilder = highlightsBuilder,
                )
            },
        ),
        modifier = modifier,
    )
}

private object CustomCoil3ImageTransformer : ImageTransformer {

    @Composable
    override fun transform(link: String): ImageData {
        return rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(link)
//                .size(coil3.size.Size.ORIGINAL)
                .build(),
            filterQuality = FilterQuality.High,
        ).let { ImageData(it) }
    }

    @Composable
    override fun intrinsicSize(painter: Painter): Size {
        var size by remember(painter) { mutableStateOf(painter.intrinsicSize) }
        if (painter is AsyncImagePainter) {
            val painterState = painter.state.collectAsState()
            val intrinsicSize = painterState.value.painter?.intrinsicSize
            intrinsicSize?.also { size = it }
        }
        return size
    }
}
