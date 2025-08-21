package io.github.ptitjes.konvo.frontend.compose.util

import androidx.compose.runtime.*
import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.flow.*
import org.kodein.di.compose.*

@Composable
fun <T, R> rememberSetting(key: SettingsKey<T>, initial: R, mapper: (T) -> R): State<R> {
    val repository by rememberInstance<SettingsRepository>()
    return repository.getSettings(key).map(mapper).collectAsState(initial)
}
