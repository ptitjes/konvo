package io.github.ptitjes.konvo.plugin.core.i18n

import io.github.ptitjes.syrup.specification.*
import org.kodein.type.*

@PublishedApi
internal object I18nStringsProviders : ExtensionPoint.Plural<I18nStringsProviders.Contribution<*>>(generic()) {
    class Contribution<T : Any>(
        val stringsType: TypeToken<T>,
        val languageTag: LanguageTag,
        val provider: () -> T,
    )
}

inline fun <reified T : Any> PluginSpecificationBuilder.i18nStrings(
    languageTag: LanguageTag,
    noinline provider: () -> T,
) {
    I18nStringsProviders {
        contribution { I18nStringsProviders.Contribution(generic<T>(), languageTag, provider) }
    }
}
