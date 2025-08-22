package io.github.ptitjes.konvo.frontend.compose.models

/**
 * Translated strings for the models package (model selector and provider settings UI).
 */
internal data class ModelStrings(
    val configuredProvidersTitle: String,
    val configuredProvidersDescription: String,
    val addProviderAria: String,
    val noProvidersMessage: String,
    val dragHandleAria: String,
    val editProviderAria: String,
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
    val nameEmptyError: String,
    val nameUniqueError: String,
    val addProviderConfirmAria: String,
    val modelLabel: String,
)
