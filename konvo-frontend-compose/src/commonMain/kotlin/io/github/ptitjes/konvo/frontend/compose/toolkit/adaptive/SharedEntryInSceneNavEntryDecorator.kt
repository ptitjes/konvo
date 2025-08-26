package io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*

/**
 * A [NavEntryDecorator] that wraps each entry in a shared element that is controlled by the [Scene].
 */
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun rememberSharedEntryInSceneNavEntryDecorator(): NavEntryDecorator<NavKey> = remember {
    navEntryDecorator<NavKey> { entry ->
        with(LocalNavSharedTransitionScope.current) {
            Box(
                Modifier.sharedElement(
                    rememberSharedContentState(entry.contentKey),
                    animatedVisibilityScope = LocalNavAnimatedContentScope.current,
                ),
            ) {
                entry.Content()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
val LocalNavSharedTransitionScope: ProvidableCompositionLocal<SharedTransitionScope> =
    compositionLocalOf {
        throw IllegalStateException(
            "Unexpected access to LocalNavSharedTransitionScope. You must provide a " +
                    "SharedTransitionScope from a call to SharedTransitionLayout() or " +
                    "SharedTransitionScope()"
        )
    }
