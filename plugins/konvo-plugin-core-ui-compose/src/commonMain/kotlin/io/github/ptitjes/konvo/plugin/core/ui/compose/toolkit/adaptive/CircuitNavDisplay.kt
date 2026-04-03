package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.navigation3.runtime.*
import androidx.navigation3.scene.*
import androidx.navigation3.ui.*
import androidx.navigationevent.*
import com.slack.circuit.foundation.*
import com.slack.circuit.runtime.*

@Composable
fun <T : NavScreen> CircuitNavDisplay(
    navigator: Navigator,
    backStack: List<T>,
    modifier: Modifier = Modifier.Companion,
    contentAlignment: Alignment = Alignment.TopStart,
    entryDecorators: List<NavEntryDecorator<T>> = listOf(
        rememberSaveableStateHolderNavEntryDecorator(),
        rememberRetainedStateHolderNavEntryDecorator(),
    ),
    sceneStrategy: SceneStrategy<T> = SinglePaneSceneStrategy(),
    sharedTransitionScope: SharedTransitionScope? = null,
    sizeTransform: SizeTransform? = null,
    transitionSpec: AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
        defaultTransitionSpec(),
    popTransitionSpec: AnimatedContentTransitionScope<Scene<T>>.() -> ContentTransform =
        defaultPopTransitionSpec(),
    predictivePopTransitionSpec:
    AnimatedContentTransitionScope<Scene<T>>.(
        @NavigationEvent.SwipeEdge Int,
    ) -> ContentTransform =
        defaultPredictivePopTransitionSpec(),
    circuit: Circuit = requireNotNull(LocalCircuit.current),
    entryProvider: (T) -> NavEntry<T>,
) {
    if (backStack.isEmpty()) return

    CircuitCompositionLocals(circuit = circuit) {
        NavDisplay(
            modifier = modifier,
            contentAlignment = contentAlignment,
            backStack = backStack,
            onBack = { navigator.pop() },
            sceneStrategy = sceneStrategy,
            sharedTransitionScope = sharedTransitionScope,
            sizeTransform = sizeTransform,
            entryDecorators = entryDecorators,
            entryProvider = entryProvider,
            transitionSpec = transitionSpec,
            popTransitionSpec = popTransitionSpec,
            predictivePopTransitionSpec = predictivePopTransitionSpec,
        )
    }
}
