package io.github.ptitjes.konvo.core.settings

import kotlinx.coroutines.flow.*
import kotlinx.serialization.*

interface SettingsRepository {
    fun <T> getSettings(key: SettingsKey<T>): StateFlow<T>
    suspend fun <T> updateSettings(key: SettingsKey<T>, value: T)
}

class SettingsKey<T>(
    val name: String,
    val defaultValue: T,
    val serializer: KSerializer<T>,
)
