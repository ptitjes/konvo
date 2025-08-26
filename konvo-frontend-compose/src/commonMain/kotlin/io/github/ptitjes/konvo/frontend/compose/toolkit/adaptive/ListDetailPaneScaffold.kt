package io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive

import androidx.compose.animation.*
import androidx.compose.material3.adaptive.*
import androidx.compose.runtime.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.*

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun <T : Any> ListDetailPaneScaffold(
    backStack: List<T>,
    onBack: (count: Int) -> Unit,
    entryProvider: (key: T) -> NavEntry<T>,
) {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val paneType = paneTypeFromAdaptiveInfo(adaptiveInfo)

    val listDetailStrategy = remember(paneType) { ListDetailStrategy<T>() }

    SharedTransitionLayout {
        CompositionLocalProvider(
            LocalListDetailPaneType provides paneType,
            LocalNavSharedTransitionScope provides this,
        ) {
            NavDisplay(
                backStack = backStack,
                onBack = onBack,
                sceneStrategy = listDetailStrategy,
                entryDecorators = listOf(
                    rememberSharedEntryInSceneNavEntryDecorator(),
                    rememberSceneSetupNavEntryDecorator(),
                    rememberSavedStateNavEntryDecorator(),
                    rememberViewModelStoreNavEntryDecorator(),
                ),
                entryProvider = entryProvider,
            )
        }
    }
}
