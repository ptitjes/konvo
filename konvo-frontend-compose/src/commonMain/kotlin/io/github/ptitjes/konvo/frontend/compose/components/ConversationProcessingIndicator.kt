package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.animation.core.*
import androidx.compose.desktop.ui.tooling.preview.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*

@Composable
fun ConversationProcessingIndicator() {
    val horizontalArrangement = Arrangement.Start

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = horizontalArrangement,
    ) {
        val dotSize = 8.dp
        val dotSpacing = 6.dp
        val baseDuration = 1200
        val delayStep = 200 // ms between dots
        val totalDuration = baseDuration + 2 * delayStep // ensure last dot stays within cycle
        val maxScale = 1.0f
        val minScale = 0.6f
        val minAlpha = 0.4f

        val transition = rememberInfiniteTransition(label = "processing-dots")

        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(dotSpacing),
        ) {
            repeat(3) { index ->
                // Stagger each dot within the same cycle using keyframe offsets
                val delay = index * delayStep
                val t0 = delay
                val t1 = t0 + baseDuration / 3
                val t2 = t0 + 2 * baseDuration / 3

                val scale by transition.animateFloat(
                    initialValue = minScale,
                    targetValue = maxScale,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = totalDuration
                            // Stay small until this dot's turn
                            minScale at 0 using LinearEasing
                            minScale at t0 using LinearEasing
                            // Grow to max
                            maxScale at t1 using LinearEasing
                            // Shrink back
                            minScale at t2 using LinearEasing
                            // Ensure we end small by the cycle end
                            minScale at totalDuration using LinearEasing
                        },
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "dot-scale-$index",
                )

                val alpha by transition.animateFloat(
                    initialValue = minAlpha,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = keyframes {
                            durationMillis = totalDuration
                            // Stay faint until this dot's turn
                            minAlpha at 0 using LinearEasing
                            minAlpha at t0 using LinearEasing
                            // Brighten
                            1f at t1 using LinearEasing
                            // Dim back
                            minAlpha at t2 using LinearEasing
                            // Ensure faint at cycle end
                            minAlpha at totalDuration using LinearEasing
                        },
                        repeatMode = RepeatMode.Restart,
                    ),
                    label = "dot-alpha-$index",
                )

                Box(
                    modifier = Modifier
                        .size(dotSize)
                        .graphicsLayer {
                            this.alpha = alpha
                            scaleX = scale
                            scaleY = scale
                        }
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape,
                        ),
                )
            }
        }
    }
}

@Preview
@Composable
private fun ConversationProcessingIndicatorPreview() {
    ConversationProcessingIndicator()
}
