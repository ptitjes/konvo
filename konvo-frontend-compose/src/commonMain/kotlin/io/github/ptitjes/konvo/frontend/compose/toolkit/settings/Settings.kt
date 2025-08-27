package io.github.ptitjes.konvo.frontend.compose.toolkit.settings

import androidx.compose.runtime.*
import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.flow.*
import org.kodein.di.*
import org.kodein.di.compose.*

@Composable
fun <T, R> rememberSetting(key: SettingsKey<T>, initial: R, mapper: (T) -> R): State<R> {
    val repository by rememberInstance<SettingsRepository>()
    return repository.getSettings(key).map(mapper).collectAsState(initial)
}

@Composable
fun <T, R> rememberSetting(key: SettingsKey<T>, mapper: (T) -> R): State<R> {
    val initialValue = with(localDI()) {
        mapper(direct.instance<SettingsRepository>().getSettings(key).value)
    }
    return rememberSetting(key, initialValue, mapper)
}
