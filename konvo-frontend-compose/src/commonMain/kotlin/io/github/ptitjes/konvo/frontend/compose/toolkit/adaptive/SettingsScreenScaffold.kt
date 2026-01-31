package io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.lifecycle.viewmodel.navigation3.*
import androidx.navigation3.runtime.*
import androidx.navigation3.ui.*

@Composable
fun <T : Any> SettingsScreenScaffold(
    backStack: List<T>,
    onBack: () -> Unit,
    entryProvider: (key: T) -> NavEntry<T>,
    modifier: Modifier = Modifier,
) {
    val windowSizeClass = currentWindowAdaptiveInfo(supportLargeAndXLargeWidth = true).windowSizeClass

    val sceneStrategy = remember(windowSizeClass) {
        ListDetailStrategy<T>(windowSizeClass = windowSizeClass)
    }

    Surface(modifier = modifier) {
        SharedTransitionLayout {
            NavDisplay(
                backStack = backStack,
                onBack = onBack,
                sceneStrategy = sceneStrategy,
                sharedTransitionScope = this,
                entryDecorators = listOf<NavEntryDecorator<T>>(
                    rememberSaveableStateHolderNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
                transitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 250)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 250))
                },
                popTransitionSpec = {
                    fadeIn(animationSpec = tween(durationMillis = 250)) togetherWith
                            fadeOut(animationSpec = tween(durationMillis = 250))
                }
            )
        }
    }
}
