package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.navigation3.scene.*

internal fun <T : Any> defaultKonvoTransitionSpec():
        AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        fadeIn(animationSpec = tween(durationMillis = 250)),
        fadeOut(animationSpec = tween(durationMillis = 250))
    )
}

internal fun <T : Any> defaultKonvoPopTransitionSpec():
        AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform = {
    ContentTransform(
        fadeIn(animationSpec = tween(durationMillis = 250)),
        fadeOut(animationSpec = tween(durationMillis = 250))
    )
}
