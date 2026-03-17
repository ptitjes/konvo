package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.translations.*
import io.github.ptitjes.syrup.specification.*

internal data class ConversationStrings(
    val untitledConversationTitle: String,

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
    val rpNoAvailableCharacters: String,
    val rpNoAvailablePersonas: String,
    val rpNoAvailableLorebooks: String,
    val rpNoAvailableModel: String,
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
    val agentWantsToCallToolPrefix: String,
    val agentCalledToolPrefix: String,
    val detailsLabel: String,
    val collapseAria: String,
    val expandAria: String,
    val successAria: String,
    val failureAria: String,
)

fun PluginSpecificationBuilder.conversationStrings() {
    i18nStrings<ConversationStrings>("ar-SA") { ArStrings.conversations }
    i18nStrings<ConversationStrings>("en-US") { EnStrings.conversations }
    i18nStrings<ConversationStrings>("es-ES") { EsStrings.conversations }
    i18nStrings<ConversationStrings>("fr-FR") { FrStrings.conversations }
    i18nStrings<ConversationStrings>("hi-IN") { HiStrings.conversations }
    i18nStrings<ConversationStrings>("zh-CN") { ZhStrings.conversations }
}
