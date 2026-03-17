package io.github.ptitjes.konvo.plugin.core.ui.compose.translations

import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.appearance.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.developer.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.mcp.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.prompts.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.text.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.tools.*

internal val EnStrings = Strings(
    navigation = NavigationStrings(
        navigationOpenAria = "Open navigation",
        navigationCloseAria = "Close navigation",
        backAria = "Back",
        detailsOpenAria = "Open details",
        detailsCloseAria = "Close details"
    ),
    agents = AgentStrings(
        agentTypeDisplayName = {
            when (it) {
                AgentType.QuestionAnswer -> "Question & Answer"
                AgentType.Roleplay -> "Role-play"
            }
        }
    ),
    models = ModelStrings(
        settingsTitle = "Model providers",
        configuredProvidersTitle = "Configured providers",
        configuredProvidersDescription = "Add, remove, and edit model providers.",
        addProviderAria = "Add provider",
        noProvidersMessage = "No model providers configured.",
        dragHandleAria = "Drag handle",
        editProviderAria = "Edit provider",
        testProviderAria = "Test provider",
        testProviderFailureAria = "Test provider failure",
        testProviderSuccessAria = "Test provider success",
        deleteProviderAria = "Delete provider",
        deleteProviderDialogTitle = "Delete provider?",
        deleteProviderDialogText = { name -> "Are you sure you want to delete \"$name\"? This cannot be undone." },
        deleteConfirm = "Delete",
        cancel = "Cancel",
        nameLabel = "Name",
        typeLabel = "Type",
        removeProviderAria = "Remove provider",
        ollamaBaseUrlLabel = "Ollama base URL",
        anthropicApiKeyLabel = "Anthropic API key",
        openAiApiKeyLabel = "OpenAI API key",
        googleApiKeyLabel = "Google API key",
        mistralAiApiKeyLabel = "Mistral AI API key",
        nameEmptyError = "Name cannot be empty",
        nameUniqueError = "Name must be unique",
        addProviderConfirmAria = "Add provider",
        modelLabel = "Model",
        testAction = "Test",
        addAction = "Add",
        saveAction = "Save",
        deleteAction = "Delete",
        testFailedMessage = { msg -> "Failed to test provider: $msg" },
    ),
    mcp = McpStrings(
        settingsTitle = "MCP servers",
        configuredServersTitle = "Configured MCP servers",
        configuredServersDescription = "Add, remove, and edit MCP servers.",
        addServerAria = "Add server",
        noServersMessage = "No MCP servers configured.",
        editServerAria = "Edit server",
        deleteServerAria = "Delete server",
        deleteServerDialogTitle = "Delete server?",
        deleteServerDialogText = { name -> "Are you sure you want to delete \"$name\"? This cannot be undone." },
        deleteConfirm = "Delete",
        cancel = "Cancel",
        nameLabel = "Name",
        transportLabel = "Transport",
        removeServerAria = "Remove server",
        sseUrlLabel = "SSE URL",
        reconnectionTimeLabel = "Reconnection time (seconds)",
        runAsProcessLabel = "Run as process",
        commandLabel = "Command (space-separated)",
        environmentLabel = "Environment (key=value; key2=value2)",
        selectorLabel = "MCP Servers",
        selectorEmpty = "No MCP servers available",
    ),
    prompts = PromptStrings(
        selectorLabel = "Prompt",
    ),
    tools = ToolStrings(
        panelLabel = "Tools",
        emptyMessage = "No tools available",
    ),
    conversations = ConversationStrings(
        untitledConversationTitle = "Untitled conversation",
        listTitle = "Conversations",
        newConversationAria = "New conversation",
        emptyTitle = "No conversations yet",
        emptyBody = "Start a new conversation to see it here.",
        startNewButton = "Start new conversation",
        openConversationAria = "Open conversation",
        conversationAria = "Conversation",
        deleteConversationAria = "Delete conversation",
        deleteDialogTitle = "Delete conversation?",
        deleteDialogText = { title -> "Are you sure you want to delete \"$title\"This action cannot be undone." },
        deleteConfirm = "Delete",
        cancel = "Cancel",
        newConversationTitle = "New Conversation",
        createAria = "Create",
        qaNoModels = "No available models",
        qaNoToolModels = "No available models with tool support",
        rpNoAvailableCharacters = "No available characters",
        rpNoAvailablePersonas = "No available personas",
        rpNoAvailableLorebooks = "No available lorebooks",
        rpNoAvailableModel = "No available models",
        personaSettingsAria = "Persona Settings",
        additionalLorebookLabel = "Additional Lorebook",
        newConversationIconAria = "New conversation",
        inputPlaceholder = "Type a message",
        sendMessageAria = "Send the message",
        addAttachmentAria = "Add an attachment",
        newMessagesLabel = "New",
        toolUseVettingTitle = "Tool use vetting",
        agentWantsToCallToolPrefix = "Agent wants to call tool ",
        agentCalledToolPrefix = "Agent called tool ",
        detailsLabel = "Details",
        collapseAria = "Collapse",
        expandAria = "Expand",
        successAria = "Success",
        failureAria = "Failure",
    ),
    appearance = AppearanceStrings(
        settingsTitle = "Appearance",
        baseColorSchemeTitle = "Base color scheme",
        baseColorSchemeDescription = "The color scheme used for the application.",
        baseColorSchemeOptionDark = "Dark",
        baseColorSchemeOptionLight = "Light",
        baseColorSchemeOptionSystem = "Adapt to system",
    ),
    developer = DeveloperStrings(
        settingsTitle = "Developer",
        openTelemetryTitle = "OpenTelemetry (gRPC)",
        openTelemetryDescription = "Export traces to an OpenTelemetry collector using gRPC.",
        openTelemetryEnabledLabel = "Enable OpenTelemetry export",
        openTelemetryEndpointLabel = "Endpoint",
        openTelemetryVerboseLabel = "Verbose export",
    ),
    settings = SettingsStrings(
        listTitle = "Settings",
        selectSectionAria = "Select settings section",
    ),
    formats = FormatStrings(
        now = "now",
    ),
)
