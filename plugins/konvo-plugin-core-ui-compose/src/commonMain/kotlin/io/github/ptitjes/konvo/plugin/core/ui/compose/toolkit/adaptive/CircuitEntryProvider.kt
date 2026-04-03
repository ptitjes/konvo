package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.navigation3.runtime.*
import com.slack.circuit.foundation.*
import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.screen.Screen
import kotlin.reflect.*

fun <T : NavScreen> circuitEntryProvider(
    navigator: Navigator,
    provider: CircuitEntryProviderScope<T>.() -> Unit,
): (T) -> NavEntry<T> = entryProvider {
    val scope = CircuitEntryProviderScope(this, navigator)
    scope.provider()
}

class CircuitEntryProviderScope<T : NavScreen>(
    private val delegate: EntryProviderScope<T>,
    private val navigator: Navigator,
) {
    fun <K : T> addEntryProvider(
        klass: KClass<out K>,
        klassContentKey: (key: @JvmSuppressWildcards K) -> Any = { defaultContentKey(it) },
        metadata: Map<String, Any> = emptyMap(),
        screen: (K) -> Screen,
    ) {
        delegate.addEntryProvider(
            klass,
            klassContentKey,
            metadata,
        ) { key -> CircuitContent(screen = screen(key), navigator = navigator) }
    }

    inline fun <reified K : T> entry(
        noinline clazzContentKey: (key: @JvmSuppressWildcards K) -> Any = { defaultContentKey(it) },
        metadata: Map<String, Any> = emptyMap(),
        noinline screen: (K) -> Screen = { it },
    ) {
        addEntryProvider(K::class, clazzContentKey, metadata, screen)
    }
}

private fun defaultContentKey(key: Any): Any = key.toString()
