package io.github.ptitjes.konvo.plugin.core.ui.compose.i18n

import androidx.compose.runtime.*
import io.github.ptitjes.konvo.plugin.core.i18n.*
import org.kodein.di.*
import org.kodein.di.compose.*

private val LocalI18nStrings: ProvidableCompositionLocal<I18nStrings> =
    staticCompositionLocalOf { error("No I18nStrings provided") }

val i18n: I18nStrings
    @Composable
    get() = LocalI18nStrings.current

@Composable
internal fun rememberI18nManager(): I18nManager {
    val di = localDI()
    return remember { di.direct.instance() }
}

@Composable
internal fun ProvideI18nStrings(
    i18nManager: I18nManager = rememberI18nManager(),
    content: @Composable () -> Unit,
) {
    val state by i18nManager.state.collectAsState()

    CompositionLocalProvider(
        LocalI18nStrings provides state.strings,
        content = content,
    )
}
