package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.compose.runtime.*
import io.github.ptitjes.konvo.plugin.core.settings.*

@Composable
fun <T> SettingsRepository.mutableSettingsOf(key: SettingsKey<T>): MutableState<T> {
    val settings by getSettings(key).collectAsState()
    val mutableState = remember(settings) { mutableStateOf(settings) }
    LaunchedEffect(mutableState.value) { updateSettings(key, mutableState.value) }
    return mutableState
}
