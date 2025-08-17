package io.github.ptitjes.konvo.core.settings

import kotlinx.coroutines.flow.*
import kotlinx.serialization.*

interface SettingsRepository {
    fun <T> getSettings(key: SettingsSectionKey<T>): StateFlow<T>
    suspend fun <T> updateSettings(key: SettingsSectionKey<T>, value: T)
}

class SettingsSectionKey<T>(
    val name: String,
    val defaultValue: T,
    val serializer: KSerializer<T>,
)
