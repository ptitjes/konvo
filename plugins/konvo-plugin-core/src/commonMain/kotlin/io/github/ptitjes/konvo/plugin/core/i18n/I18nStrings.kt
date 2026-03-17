package io.github.ptitjes.konvo.plugin.core.i18n

import org.kodein.type.*

abstract class I18nStrings {
    inline fun <reified T : Any> byType() = byType(generic<T>())

    abstract fun <T> byType(stringsType: TypeToken<T>): T
}
