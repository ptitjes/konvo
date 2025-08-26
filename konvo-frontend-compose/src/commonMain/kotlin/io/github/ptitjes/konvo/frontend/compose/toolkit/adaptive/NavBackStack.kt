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
import androidx.compose.runtime.snapshots.*
import androidx.savedstate.*
import androidx.savedstate.serialization.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

@OptIn(InternalSerializationApi::class)
@Composable
inline fun <reified T : NavKey> rememberNavBackStack(vararg elements: T): SnapshotStateList<T> {
    return rememberSaveable(serializer<T>()) {
        elements.toList().toMutableStateList()
    }
}

@Composable
fun <T : NavKey> rememberSaveable(
    elementSerializer: KSerializer<T>,
    init: () -> SnapshotStateList<T>,
): SnapshotStateList<T> {
    return rememberSaveable(
        saver = serializableSaver(SnapshotStateListSerializer(elementSerializer)),
        key = null,
        init = init,
    )
}

private class SnapshotStateListSerializer<T>(private val elementSerializer: KSerializer<T>) :
    KSerializer<SnapshotStateList<T>> {

    private val base = ListSerializer(elementSerializer)

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        SerialDescriptor("androidx.compose.runtime.SnapshotStateList", base.descriptor)

    override fun serialize(encoder: Encoder, value: SnapshotStateList<T>) {
        encoder.encodeSerializableValue(base, value)
    }

    override fun deserialize(decoder: Decoder): SnapshotStateList<T> {
        val deserialized = decoder.decodeSerializableValue(base)
        return SnapshotStateList<T>().apply { addAll(deserialized.toList()) }
    }
}

private fun <Serializable : Any> serializableSaver(
    serializer: KSerializer<Serializable>,
    configuration: SavedStateConfiguration = SavedStateConfiguration.DEFAULT,
): Saver<Serializable, SavedState> {
    return Saver(
        save = { original -> encodeToSavedState(serializer, original, configuration) },
        restore = { savedState -> decodeFromSavedState(serializer, savedState, configuration) },
    )
}
