package io.github.ptitjes.konvo.frontend.compose.translations

import cafe.adriel.lyricist.*
import io.github.ptitjes.konvo.frontend.compose.*
import io.github.ptitjes.konvo.frontend.compose.agents.*
import io.github.ptitjes.konvo.frontend.compose.conversations.*
import io.github.ptitjes.konvo.frontend.compose.mcp.*
import io.github.ptitjes.konvo.frontend.compose.models.*
import io.github.ptitjes.konvo.frontend.compose.prompts.*
import io.github.ptitjes.konvo.frontend.compose.roleplay.*
import io.github.ptitjes.konvo.frontend.compose.settings.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.text.*
import io.github.ptitjes.konvo.frontend.compose.tools.*

@LyricistStrings(languageTag = "zh-CN")
internal val ZhStrings = Strings(
    agents = AgentStrings(
        agentTypeDisplayName = {
            when (it) {
                AgentType.QuestionAnswer -> "问答"
                AgentType.Roleplay -> "角色扮演"
            }
        }
    ),
    models = ModelStrings(
        configuredProvidersTitle = "已配置的提供商",
        configuredProvidersDescription = "添加、删除并编辑模型提供商。",
        addProviderAria = "添加提供商",
        noProvidersMessage = "尚未配置模型提供商。",
        dragHandleAria = "拖动手柄",
        editProviderAria = "编辑提供商",
        deleteProviderAria = "删除提供商",
        deleteProviderDialogTitle = "删除提供商？",
        deleteProviderDialogText = { name -> "确定要删除\"$name\"吗？该操作无法撤销。" },
        deleteConfirm = "删除",
        cancel = "取消",
        nameLabel = "名称",
        typeLabel = "类型",
        removeProviderAria = "移除提供商",
        ollamaBaseUrlLabel = "Ollama 基础 URL",
        anthropicApiKeyLabel = "Anthropic API 密钥",
        openAiApiKeyLabel = "OpenAI API 密钥",
        googleApiKeyLabel = "Google API 密钥",
        nameEmptyError = "名称不能为空",
        nameUniqueError = "名称必须唯一",
        addProviderConfirmAria = "添加提供商",
        modelLabel = "模型",
    ),
    mcp = McpStrings(
        configuredServersTitle = "已配置的 MCP 服务器",
        configuredServersDescription = "添加、删除并编辑 MCP 服务器。",
        addServerAria = "添加服务器",
        noServersMessage = "尚未配置 MCP 服务器。",
        editServerAria = "编辑服务器",
        deleteServerAria = "删除服务器",
        deleteServerDialogTitle = "删除服务器？",
        deleteServerDialogText = { name -> "确定要删除\"$name\"吗？该操作无法撤销。" },
        deleteConfirm = "删除",
        cancel = "取消",
        nameLabel = "名称",
        transportLabel = "传输方式",
        removeServerAria = "移除服务器",
        sseUrlLabel = "SSE URL",
        reconnectionTimeLabel = "重连时间（秒）",
        runAsProcessLabel = "作为进程运行",
        commandLabel = "命令（以空格分隔）",
        environmentLabel = "环境（key=value; key2=value2）",
        selectorLabel = "MCP 服务器",
        selectorEmpty = "没有可用的 MCP 服务器",
    ),
    prompts = PromptStrings(
        selectorLabel = "提示",
    ),
    roleplay = RoleplayStrings(
        deleteConfirm = "删除",
        cancel = "取消",
        personaLabel = "人设",
        personasTitle = "人设",
        personasDescription = "添加、删除并编辑人设。",
        addPersonaAria = "添加人设",
        noPersonasConfigured = "尚未配置人设。",
        nicknamePrefix = { nickname -> "昵称：$nickname" },
        withLorebook = "带有设定集",
        editPersonaAria = "编辑人设",
        deletePersonaAria = "删除人设",
        deletePersonaDialogTitle = "删除人设？",
        deletePersonaDialogText = { name -> "确定要删除\"$name\"吗？该操作无法撤销。" },
        nameLabel = "名称",
        nicknameLabel = "昵称",
        defaultLorebookLabel = "默认设定集",
        saveAction = "保存",
        addAction = "添加",
        removePersonaAria = "移除人设",
        lorebookLabel = "设定集",
        lorebookNone = "无",
        lorebookUnnamed = "未命名设定集",
        importedLorebooksTitle = "已导入设定集",
        importedLorebooksDescription = "导入、列出并删除设定集。",
        importLorebookAria = "导入设定集",
        failedToLoadLorebooks = { msg -> "加载设定集失败：$msg" },
        noLorebooksAvailable = "没有可用的设定集。",
        deleteLorebookDialogTitle = "删除设定集？",
        deleteLorebookDialogText = { name -> "确定要删除\"$name\"吗？该操作无法撤销。" },
        deleteLorebookAria = "删除设定集",
        characterTagsFilterTitle = "角色标签过滤",
        characterTagsFilterDescription = "列在此处的标签在显示角色时将被排除。使用逗号分隔多个标签。",
        characterTagsPlaceholder = "例如：nsfw, beta, wip",
        importedCharactersTitle = "已导入角色",
        importedCharactersDescription = "导入、列出并删除角色。",
        importCharactersAria = "导入角色",
        failedToLoadCharacters = { msg -> "加载角色失败：$msg" },
        noCharactersAvailable = "没有可用的角色。",
        deleteCharacterDialogTitle = "删除角色？",
        deleteCharacterDialogText = { name -> "确定要删除\"$name\"吗？该操作无法撤销。" },
        deleteCharacterAria = "删除角色",
        hasCharacterBookAria = "包含角色设定",
        defaultPersonaTitle = "默认人设",
        defaultPersonaDescription = "在新建角色扮演会话中用作你的人设。",
        noPersonaDefined = "尚未定义人设",
        defaultPreferredModelTitle = "默认首选模型",
        defaultPreferredModelDescription = "在新的角色扮演会话中默认使用的模型。",
        noAvailableModels = "没有可用的模型",
        defaultSystemPromptTitle = "默认系统提示",
        defaultSystemPromptDescription = "当角色卡未定义系统提示时使用。",
        defaultLorebookSettingsTitle = "默认设定集设置",
        defaultLorebookSettingsDescription = "当角色卡未定义设定集配置时使用。",
        scanDepthLabel = "扫描深度",
        tokenBudgetLabel = "Token 预算",
        recursiveScanningLabel = "递归扫描",
        greetingLabel = "问候语",
        randomGreeting = "随机问候语",
        greetingOptionLabel = { index, preview -> "问候语${index + 1}：$preview" },
    ),
    tools = ToolStrings(
        panelLabel = "工具",
        emptyMessage = "没有可用的工具",
    ),
    conversations = ConversationStrings(
        backAria = "返回",
        settingsAria = "设置",
        listTitle = "会话",
        newConversationAria = "新建会话",
        emptyTitle = "暂时还没有会话",
        emptyBody = "开始一个新会话，它会显示在这里。",
        startNewButton = "开始新会话",
        openConversationAria = "打开会话",
        conversationAria = "会话",
        deleteConversationAria = "删除会话",
        deleteDialogTitle = "删除会话？",
        deleteDialogText = { title -> "确定要删除\"$title\"吗？该操作无法撤销。" },
        deleteConfirm = "删除",
        cancel = "取消",
        newConversationTitle = "新建会话",
        createAria = "创建",
        qaNoModels = "没有可用的模型",
        qaNoToolModels = "没有支持工具的可用模型",
        rpNoCharactersOrModels = "没有可用的角色或模型",
        personaSettingsAria = "人设设置",
        additionalLorebookLabel = "附加设定集",
        newConversationIconAria = "新建会话",
        inputPlaceholder = "输入消息",
        sendMessageAria = "发送消息",
        addAttachmentAria = "添加附件",
        newMessagesLabel = "新",
        toolUseVettingTitle = "工具使用审核",
        agentCalledToolPrefix = "代理调用了工具：",
        detailsLabel = "详情",
        collapseAria = "折叠",
        expandAria = "展开",
        successAria = "成功",
        failureAria = "失败",
    ),
    settings = SettingsStrings(
        listTitle = "设置",
        selectSectionAria = "选择设置部分",
        sectionTitles = mapOf(
            "appearance" to "外观",
            "mcp" to "MCP 服务器",
            "models" to "模型提供商",
            "roleplay" to "角色扮演",
            "characters" to "角色",
            "lorebooks" to "设定集",
            "personas" to "人设",
        ),
        appearanceBaseColorSchemeTitle = "基础配色方案",
        appearanceBaseColorSchemeDescription = "应用使用的配色方案。",
        appearanceBaseColorSchemeOptionDark = "深色",
        appearanceBaseColorSchemeOptionLight = "浅色",
        appearanceBaseColorSchemeOptionSystem = "跟随系统",
    ),
    navigationDestinationTitles = { state ->
        when (state) {
            AppState.Conversations -> "会话"
            AppState.Archive -> "归档"
            AppState.KnowledgeBases -> "知识库"
            AppState.Settings -> "设置"
        }
    },
    formats = FormatStrings(
        now = "现在",
    ),
)
