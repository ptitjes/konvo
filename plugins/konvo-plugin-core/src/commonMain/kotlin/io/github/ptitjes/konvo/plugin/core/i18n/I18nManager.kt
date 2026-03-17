package io.github.ptitjes.konvo.plugin.core.i18n

import io.github.ptitjes.syrup.*
import kotlinx.coroutines.flow.*
import org.kodein.type.*
import kotlin.concurrent.atomics.*

private const val DEFAULT_LANGUAGE_TAG: LanguageTag = "en-US"

class I18nManager(
    // TODO use the SettingsManager to get the current language tag
    pluginContext: PluginContext,
) {
    private val stringsProviders by pluginContext.contributions(I18nStringsProviders)

    private val indexed: Map<LanguageTag, Map<TypeToken<*>, () -> Any>> by lazy {
        stringsProviders.groupBy { it.languageTag }.mapValues { (_, providers) ->
            providers.associateBy { it.stringsType }.mapValues { (_, provider) -> provider.provider }
        }
    }

    private val initialLanguageTag: LanguageTag = getPlatformLanguageTag()
    private val defaultLanguageTag: LanguageTag = DEFAULT_LANGUAGE_TAG
    private val mutableState: MutableStateFlow<I18nState> =
        MutableStateFlow(I18nState(initialLanguageTag, getStrings(initialLanguageTag)))

    val state: StateFlow<I18nState> = mutableState.asStateFlow()

    var languageTag: LanguageTag
        get() = mutableState.value.languageTag
        set(languageTag) {
            mutableState.value = I18nState(languageTag, getStrings(languageTag))
        }

    val strings: I18nStrings
        get() = mutableState.value.strings

    private val LanguageTag.fallback: LanguageTag
        get() = split(FALLBACK_REGEX).first()

    private fun getStrings(languageTag: LanguageTag): I18nStrings = I18nStringsImpl(
        indexed[languageTag]
            ?: indexed[languageTag.fallback]
            ?: indexed[defaultLanguageTag]
            ?: error("Strings for language tag $languageTag not found")
    )

    @OptIn(ExperimentalAtomicApi::class)
    private class I18nStringsImpl(private val perStringsTypeStrings: Map<TypeToken<*>, () -> Any>) : I18nStrings() {
        private fun <T> providerForType(type: TypeToken<T>): () -> Any =
            perStringsTypeStrings[type] ?: error("Strings for type $type not found")

        private val cachedStrings = AtomicReference(mapOf<TypeToken<*>, Any>())

        private fun <T> cachedStrings(type: TypeToken<T>): Any =
            cachedStrings.updateAndFetch { map -> map + (type to providerForType(type).invoke()) }[type]!!

        override fun <T> byType(stringsType: TypeToken<T>): T {
            @Suppress("UNCHECKED_CAST")
            return cachedStrings(stringsType) as T
        }
    }

    private companion object {
        private val FALLBACK_REGEX = Regex("[-_]")
    }
}

@ConsistentCopyVisibility
data class I18nState internal constructor(
    val languageTag: LanguageTag,
    val strings: I18nStrings,
)
