package io.github.ptitjes.konvo.frontend.compose.translations

import cafe.adriel.lyricist.*
import io.github.ptitjes.konvo.frontend.compose.agents.*
import io.github.ptitjes.konvo.frontend.compose.mcp.*
import io.github.ptitjes.konvo.frontend.compose.models.*
import io.github.ptitjes.konvo.frontend.compose.prompts.*
import io.github.ptitjes.konvo.frontend.compose.tools.*

@LyricistStrings(languageTag = "en-US", default = true)
internal val EnStrings = Strings(
    agents = AgentStrings(
        agentTypeDisplayName = {
            when (it) {
                AgentType.QuestionAnswer -> "Question & Answer"
                AgentType.Roleplay -> "Role-play"
            }
        }
    ),
    models = ModelStrings(
        configuredProvidersTitle = "Configured providers",
        configuredProvidersDescription = "Add, remove, and edit model providers.",
        addProviderAria = "Add provider",
        noProvidersMessage = "No model providers configured.",
        dragHandleAria = "Drag handle",
        editProviderAria = "Edit provider",
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
        nameEmptyError = "Name cannot be empty",
        nameUniqueError = "Name must be unique",
        addProviderConfirmAria = "Add provider",
        modelLabel = "Model",
    ),
    mcp = McpStrings(
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
    tools = ToolsStrings(
        panelLabel = "Tools",
        emptyMessage = "No tools available",
    ),
)
