package io.github.ptitjes.konvo.plugin.core.i18n

import java.util.*

internal actual fun getPlatformLanguageTag(): LanguageTag = Locale.getDefault().toLanguageTag()
