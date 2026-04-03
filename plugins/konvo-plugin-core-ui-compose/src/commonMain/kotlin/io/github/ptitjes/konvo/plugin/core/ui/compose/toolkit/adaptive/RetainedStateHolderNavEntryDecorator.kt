package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.runtime.*
import androidx.navigation3.runtime.*
import com.slack.circuit.retained.*

/**
 * Returns a [RetainedStateHolderNavEntryDecorator] that is remembered across recompositions.
 *
 * @param retainedStateHolder the [RetainedStateHolder] that scopes the returned NavEntryDecorator
 */
@Composable
fun <T : Any> rememberRetainedStateHolderNavEntryDecorator(
    retainedStateHolder: RetainedStateHolder = rememberRetainedStateHolder(),
): RetainedStateHolderNavEntryDecorator<T> =
    remember(retainedStateHolder) { RetainedStateHolderNavEntryDecorator(retainedStateHolder) }

/**
 * Wraps the content of a [NavEntry] with a [RetainedStateHolder.RetainedStateProvider] to ensure
 * that calls to [rememberRetained] within the content work properly and that state can be retained.
 * Also provides the content of a [NavEntry] with a [RetainedStateRegistry] which can be accessed
 * in the content with [LocalRetainedStateRegistry].
 *
 * This [NavEntryDecorator] is the only one that is **required** as retaining state is considered a
 * non-optional feature.
 *
 * @param retainedStateHolder the [RetainedStateHolder] that holds the state defined with
 *   [rememberRetained]. A retained state can only be restored from the [RetainedStateHolder] that it
 *   was retained with.
 */
class RetainedStateHolderNavEntryDecorator<T : Any>(
    retainedStateHolder: RetainedStateHolder,
) :
    NavEntryDecorator<T>(
        onPop = { contentKey -> retainedStateHolder.removeState(contentKey.toString()) },
        decorate = { entry ->
            retainedStateHolder.RetainedStateProvider(entry.contentKey.toString()) { entry.Content() }
        },
    )
