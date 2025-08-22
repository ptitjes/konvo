package io.github.ptitjes.konvo.frontend.compose.roleplay

/**
 * Translated strings for the roleplay package (personas, characters, lorebooks, and settings UI).
 */
internal data class RoleplayStrings(
    // Generic/common
    val deleteConfirm: String,
    val cancel: String,

    // Persona selector & editor
    val personaLabel: String,
    val personasTitle: String,
    val personasDescription: String,
    val addPersonaAria: String,
    val noPersonasConfigured: String,
    val nicknamePrefix: (String) -> String,
    val withLorebook: String,
    val editPersonaAria: String,
    val deletePersonaAria: String,
    val deletePersonaDialogTitle: String,
    val deletePersonaDialogText: (String) -> String,
    val nameLabel: String,
    val nicknameLabel: String,
    val defaultLorebookLabel: String,
    val saveAction: String,
    val addAction: String,
    val removePersonaAria: String,

    // Lorebook selector & settings
    val lorebookLabel: String,
    val lorebookNone: String,
    val lorebookUnnamed: String,
    val importedLorebooksTitle: String,
    val importedLorebooksDescription: String,
    val importLorebookAria: String,
    val failedToLoadLorebooks: (String) -> String,
    val noLorebooksAvailable: String,
    val deleteLorebookDialogTitle: String,
    val deleteLorebookDialogText: (String) -> String,
    val deleteLorebookAria: String,

    // Character settings
    val characterTagsFilterTitle: String,
    val characterTagsFilterDescription: String,
    val characterTagsPlaceholder: String,
    val importedCharactersTitle: String,
    val importedCharactersDescription: String,
    val importCharactersAria: String,
    val failedToLoadCharacters: (String) -> String,
    val noCharactersAvailable: String,
    val deleteCharacterDialogTitle: String,
    val deleteCharacterDialogText: (String) -> String,
    val deleteCharacterAria: String,
    val hasCharacterBookAria: String,

    // Roleplay settings
    val defaultPersonaTitle: String,
    val defaultPersonaDescription: String,
    val noPersonaDefined: String,
    val defaultPreferredModelTitle: String,
    val defaultPreferredModelDescription: String,
    val noAvailableModels: String,
    val defaultSystemPromptTitle: String,
    val defaultSystemPromptDescription: String,
    val defaultLorebookSettingsTitle: String,
    val defaultLorebookSettingsDescription: String,
    val scanDepthLabel: String,
    val tokenBudgetLabel: String,
    val recursiveScanningLabel: String,

    // Character greeting selector
    val greetingLabel: String,
    val randomGreeting: String,
    val greetingOptionLabel: (Int, String) -> String,
)
