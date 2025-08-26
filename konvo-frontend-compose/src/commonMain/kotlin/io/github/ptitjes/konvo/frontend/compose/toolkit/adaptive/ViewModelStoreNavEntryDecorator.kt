/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.ptitjes.konvo.frontend.compose.toolkit.adaptive

import androidx.compose.runtime.*
import androidx.lifecycle.*
import androidx.lifecycle.viewmodel.*
import androidx.lifecycle.viewmodel.compose.*
import androidx.savedstate.*

/**
 * Returns a [ViewModelStoreNavEntryDecorator] that is remembered across recompositions.
 *
 * @param [viewModelStoreOwner] The [ViewModelStoreOwner] that provides the [ViewModelStore] to
 *   NavEntries
 * @param [shouldRemoveStoreOwner] A lambda that returns a Boolean for whether the store owner for a
 *   [NavEntry] should be cleared when the [NavEntry] is popped from the backStack. If true, the
 *   entry's ViewModelStoreOwner will be removed.
 */
@Composable
fun rememberViewModelStoreNavEntryDecorator(
    viewModelStoreOwner: ViewModelStoreOwner =
        checkNotNull(LocalViewModelStoreOwner.current) {
            "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
        },
    shouldRemoveStoreOwner: () -> Boolean = { true },
): NavEntryDecorator<Any> = remember {
    ViewModelStoreNavEntryDecorator(viewModelStoreOwner.viewModelStore, shouldRemoveStoreOwner)
}

/**
 * Provides the content of a [NavEntry] with a [ViewModelStoreOwner] and provides that
 * [ViewModelStoreOwner] as a [LocalViewModelStoreOwner] so that it is available within the content.
 *
 * This requires the usage of [androidx.navigation3.runtime.SavedStateNavEntryDecorator] to ensure
 * that the [NavEntry] scoped [ViewModel]s can properly provide access to
 * [androidx.lifecycle.SavedStateHandle]s
 *
 * @param [viewModelStore] The [ViewModelStore] that provides to NavEntries
 * @param [shouldRemoveStoreOwner] A lambda that returns a Boolean for whether the store owner for a
 *   [NavEntry] should be cleared when the [NavEntry] is popped from the backStack. If true, the
 *   entry's ViewModelStoreOwner will be removed.
 */
fun ViewModelStoreNavEntryDecorator(
    viewModelStore: ViewModelStore,
    shouldRemoveStoreOwner: () -> Boolean,
): NavEntryDecorator<Any> {
    val storeOwnerProvider: EntryViewModel = viewModelStore.getEntryViewModel()
    val onPop: (Any) -> Unit = { key ->
        if (shouldRemoveStoreOwner()) {
            storeOwnerProvider.clearViewModelStoreOwnerForKey(key)
        }
    }
    return navEntryDecorator(onPop) { entry ->
        val viewModelStore = storeOwnerProvider.viewModelStoreForKey(entry.contentKey)

        val childViewModelStoreOwner = remember {
            object :
                ViewModelStoreOwner,
                HasDefaultViewModelProviderFactory {
                override val viewModelStore: ViewModelStore
                    get() = viewModelStore

                override val defaultViewModelProviderFactory: ViewModelProvider.Factory
                    get() = SavedStateViewModelFactory()

                override val defaultViewModelCreationExtras: CreationExtras
                    get() =
                        MutableCreationExtras().also {
                            it[VIEW_MODEL_STORE_OWNER_KEY] = this
                        }
            }
        }
        CompositionLocalProvider(LocalViewModelStoreOwner provides childViewModelStoreOwner) {
            entry.Content()
        }
    }
}

private class EntryViewModel : ViewModel() {
    private val owners = mutableMapOf<Any, ViewModelStore>()

    fun viewModelStoreForKey(key: Any): ViewModelStore = owners.getOrPut(key) { ViewModelStore() }

    fun clearViewModelStoreOwnerForKey(key: Any) {
        owners.remove(key)?.clear()
    }

    override fun onCleared() {
        owners.forEach { (_, store) -> store.clear() }
    }
}

private fun ViewModelStore.getEntryViewModel(): EntryViewModel {
    val provider =
        ViewModelProvider.create(
            store = this,
            factory = viewModelFactory { initializer { EntryViewModel() } },
        )
    return provider[EntryViewModel::class]
}

/** The CompositionLocal containing the current [SavedStateRegistryOwner]. */
val LocalSavedStateRegistryOwner: ProvidableCompositionLocal<SavedStateRegistryOwner> =
    staticCompositionLocalOf {
        error("CompositionLocal LocalSavedStateRegistryOwner not present")
    }
