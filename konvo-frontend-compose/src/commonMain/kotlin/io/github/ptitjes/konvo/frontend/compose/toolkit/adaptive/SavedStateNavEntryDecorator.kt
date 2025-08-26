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
import androidx.compose.runtime.saveable.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.savedstate.*

/**
 * Returns a [SavedStateNavEntryDecorator] that is remembered across recompositions.
 *
 * @param saveableStateHolder the [SaveableStateHolder] that scopes the returned NavEntryDecorator
 */
@Composable
fun rememberSavedStateNavEntryDecorator(
    saveableStateHolder: SaveableStateHolder = rememberSaveableStateHolder(),
): NavEntryDecorator<Any> = remember { SavedStateNavEntryDecorator(saveableStateHolder) }

/**
 * Wraps the content of a [NavEntry] with a [SaveableStateHolder.SaveableStateProvider] to ensure
 * that calls to [rememberSaveable] within the content work properly and that state can be saved.
 * Also provides the content of a [NavEntry] with a [SavedStateRegistryOwner] which can be accessed
 * in the content with [LocalSavedStateRegistryOwner].
 *
 * This [NavEntryDecorator] is the only one that is **required** as saving state is considered a
 * non-optional feature.
 *
 * @param saveableStateHolder the [SaveableStateHolder] that holds the state defined with
 *   [rememberSaveable]. A saved state can only be restored from the [SaveableStateHolder] that it
 *   was saved with.
 */
fun SavedStateNavEntryDecorator(
    saveableStateHolder: SaveableStateHolder,
): NavEntryDecorator<Any> {
    val onPop: (Any) -> Unit = { contentKey -> saveableStateHolder.removeState(contentKey) }

    return navEntryDecorator(onPop = onPop) { entry ->
        saveableStateHolder.SaveableStateProvider(entry.contentKey) { entry.Content() }
    }
}
