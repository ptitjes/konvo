package io.github.ptitjes.konvo.plugin.core.ui.compose.models

/**
 * Translated strings for the models package (model selector and provider settings UI).
 */
internal data class ModelStrings(
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
    // New labels for bottom sheet actions and feedback
    val testAction: String,
    val addAction: String,
    val saveAction: String,
    val deleteAction: String,
    val testFailedMessage: (String) -> String,
)
