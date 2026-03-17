package io.github.ptitjes.konvo.plugin.core.ui.compose.translations

import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.appearance.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.mcp.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.prompts.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.text.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.tools.*

internal val ZhStrings = Strings(
    navigation = NavigationStrings(
        navigationOpenAria = "打开导航",
        navigationCloseAria = "关闭导航",
        backAria = "返回",
        detailsOpenAria = "打开详情",
        detailsCloseAria = "关闭详情",
    ),
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
        testProviderAria = "测试提供商",
        testProviderFailureAria = "测试提供商失败",
        testProviderSuccessAria = "测试提供商成功",
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
        mistralAiApiKeyLabel = "Mistral AI API 密钥",
        nameEmptyError = "名称不能为空",
        nameUniqueError = "名称必须唯一",
        addProviderConfirmAria = "添加提供商",
        modelLabel = "模型",
        testAction = "测试",
        addAction = "添加",
        saveAction = "保存",
        deleteAction = "删除",
        testFailedMessage = { msg -> "测试提供商失败：$msg" },
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
    tools = ToolStrings(
        panelLabel = "工具",
        emptyMessage = "没有可用的工具",
    ),
    conversations = ConversationStrings(
        untitledConversationTitle = "未命名会话",
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
        rpNoAvailableCharacters = "没有可用的角色",
        rpNoAvailablePersonas = "没有可用的人设",
        rpNoAvailableLorebooks = "没有可用的设定集",
        rpNoAvailableModel = "没有可用的模型",
        personaSettingsAria = "人设设置",
        additionalLorebookLabel = "附加设定集",
        newConversationIconAria = "新建会话",
        inputPlaceholder = "输入消息",
        sendMessageAria = "发送消息",
        addAttachmentAria = "添加附件",
        newMessagesLabel = "新",
        toolUseVettingTitle = "工具使用审核",
        agentWantsToCallToolPrefix = "代理想要调用工具 ",
        agentCalledToolPrefix = "代理调用了工具 ",
        detailsLabel = "详情",
        collapseAria = "折叠",
        expandAria = "展开",
        successAria = "成功",
        failureAria = "失败",
    ),
    appearance = AppearanceStrings(
        baseColorSchemeTitle = "基础配色方案",
        baseColorSchemeDescription = "应用使用的配色方案。",
        baseColorSchemeOptionDark = "深色",
        baseColorSchemeOptionLight = "浅色",
        baseColorSchemeOptionSystem = "跟随系统",
    ),
    settings = SettingsStrings(
        listTitle = "设置",
        selectSectionAria = "选择设置部分",
        sectionTitles = mapOf(
            "appearance" to "外观",
            "mcp" to "MCP 服务器",
            "models" to "模型提供商",
            "developer" to "开发者",
        ),
        developerOpenTelemetryTitle = "OpenTelemetry (gRPC)",
        developerOpenTelemetryDescription = "使用 gRPC 将追踪导出到 OpenTelemetry 收集器。",
        developerOpenTelemetryEnabledLabel = "启用 OpenTelemetry 导出",
        developerOpenTelemetryEndpointLabel = "端点",
        developerOpenTelemetryVerboseLabel = "详细导出",
    ),
    formats = FormatStrings(
        now = "现在",
    ),
)
