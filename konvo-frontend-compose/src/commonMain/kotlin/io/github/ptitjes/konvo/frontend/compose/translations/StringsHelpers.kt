package io.github.ptitjes.konvo.frontend.compose.translations

import androidx.compose.runtime.*
import androidx.compose.ui.text.intl.*
import cafe.adriel.lyricist.*

internal val translations: Map<LanguageTag, Strings> = mapOf(
    "en-US" to EnStrings,
    "fr-FR" to FrStrings,
    "zh-CN" to ZhStrings,
    "hi-IN" to HiStrings,
    "es-ES" to EsStrings,
)

internal val LocalStrings: ProvidableCompositionLocal<Strings> =
    staticCompositionLocalOf { EnStrings }

internal val strings: Strings
    @Composable
    get() = LocalStrings.current

@Composable
internal fun rememberStrings(
    defaultLanguageTag: LanguageTag = "en-US",
    currentLanguageTag: LanguageTag = Locale.current.toLanguageTag(),
): Lyricist<Strings> =
    rememberStrings(translations, defaultLanguageTag, currentLanguageTag)

@Composable
internal fun ProvideStrings(
    lyricist: Lyricist<Strings> = rememberStrings(),
    content: @Composable () -> Unit,
) {
    ProvideStrings(lyricist, LocalStrings, content)
}
