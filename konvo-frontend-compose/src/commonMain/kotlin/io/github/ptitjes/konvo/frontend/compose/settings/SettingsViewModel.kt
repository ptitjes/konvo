package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    fun <T> getSettings(key: SettingsKey<T>): StateFlow<SettingsViewState<T>> {
        @Suppress("UNCHECKED_CAST")
        return settingsRepository.getSettings(key)
            .map { SettingsViewState.Loaded(it) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Lazily,
                initialValue = SettingsViewState.Loading as SettingsViewState<T>,
            )
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> updateSettings(key: SettingsKey<T>, updater: (previous: T) -> T) = viewModelScope.launch {
        val settings = getSettings(key).filterIsInstance<SettingsViewState.Loaded<T>>().first().value
        settingsRepository.updateSettings(key, updater(settings))
    }
}

sealed interface SettingsViewState<T> {
    data object Loading : SettingsViewState<Any>
    data class Loaded<T>(val value: T) : SettingsViewState<T>
}
