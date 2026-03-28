package io.github.ptitjes.konvo.plugin.core.ui.compose.models

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*

val I18nStrings.models: ModelStrings get() = byType()

/**
 * Translated strings for the models package (model selector and provider settings UI).
 */
data class ModelStrings(
    val settingsTitle: String,
    val configuredProvidersTitle: String,
    val configuredProvidersDescription: String,
    val addProviderAria: String,
    val noProvidersMessage: String,
    val dragHandleAria: String,
    val editProviderAria: String,
    val testProviderAria: String,
    val testProviderSuccessAria: String,
    val testProviderFailureAria: String,
    val deleteProviderAria: String,
    val deleteProviderDialogTitle: String,
    val deleteProviderDialogText: (String) -> String,
    val deleteConfirm: String,
    val cancel: String,
    val nameLabel: String,
    val typeLabel: String,
    val removeProviderAria: String,
    val ollamaBaseUrlLabel: String,
    val anthropicApiKeyLabel: String,
    val openAiApiKeyLabel: String,
    val googleApiKeyLabel: String,
    val mistralAiApiKeyLabel: String,
    val nameEmptyError: String,
    val nameUniqueError: String,
    val addProviderConfirmAria: String,
    val modelLabel: String,
    val noAvailableModelsText: String,
    // New labels for bottom sheet actions and feedback
    val testAction: String,
    val addAction: String,
    val saveAction: String,
    val deleteAction: String,
    val testFailedMessage: (String) -> String,
)

fun PluginSpecificationBuilder.modelStrings() {
    i18nStrings<ModelStrings>("ar-SA") { ArStrings.models }
    i18nStrings<ModelStrings>("en-US") { EnStrings.models }
    i18nStrings<ModelStrings>("es-ES") { EsStrings.models }
    i18nStrings<ModelStrings>("fr-FR") { FrStrings.models }
    i18nStrings<ModelStrings>("hi-IN") { HiStrings.models }
    i18nStrings<ModelStrings>("zh-CN") { ZhStrings.models }
}
