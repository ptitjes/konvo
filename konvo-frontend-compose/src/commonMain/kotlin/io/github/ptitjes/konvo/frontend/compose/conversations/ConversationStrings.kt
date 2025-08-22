package io.github.ptitjes.konvo.frontend.compose.conversations

internal data class ConversationStrings(
    // General/navigation
    val backAria: String,
    val settingsAria: String,

    // Conversation list
    val listTitle: String,
    val newConversationAria: String,

    // Empty list
    val emptyTitle: String,
    val emptyBody: String,
    val startNewButton: String,

    // Conversation item
    val openConversationAria: String,
    val conversationAria: String,
    val deleteConversationAria: String,
    val deleteDialogTitle: String,
    val deleteDialogText: (title: String) -> String,
    val deleteConfirm: String,
    val cancel: String,

    // New conversation screen
    val newConversationTitle: String,
    val createAria: String,
    val qaNoModels: String,
    val qaNoToolModels: String,
    val rpNoCharactersOrModels: String,
    val personaSettingsAria: String,
    val additionalLorebookLabel: String,
    val newConversationIconAria: String,

    // User input
    val inputPlaceholder: String,
    val sendMessageAria: String,

    // Attachments
    val addAttachmentAria: String,

    // Misc
    val newMessagesLabel: String,

    // Tool use notifications
    val toolUseVettingTitle: String,
    val agentCalledToolPrefix: String,
    val detailsLabel: String,
    val collapseAria: String,
    val expandAria: String,
    val successAria: String,
    val failureAria: String,
)
