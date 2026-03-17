package io.github.ptitjes.konvo.plugin.core.ui.compose.translations

import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.appearance.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.developer.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.mcp.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.prompts.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.text.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.tools.*

internal val HiStrings = Strings(
    navigation = NavigationStrings(
        navigationOpenAria = "नेविगेशन खोलें",
        navigationCloseAria = "नेविगेशन बंद करें",
        backAria = "वापस",
        detailsOpenAria = "विवरण खोलें",
        detailsCloseAria = "विवरण बंद करें",
    ),
    agents = AgentStrings(
        agentTypeDisplayName = {
            when (it) {
                AgentType.QuestionAnswer -> "प्रश्न और उत्तर"
                AgentType.Roleplay -> "भूमिका निभाना"
            }
        }
    ),
    models = ModelStrings(
        configuredProvidersTitle = "कॉन्फ़िगर किए गए प्रदाता",
        configuredProvidersDescription = "मॉडल प्रदाताओं को जोड़ें, हटाएँ और संपादित करें।",
        addProviderAria = "प्रदाता जोड़ें",
        noProvidersMessage = "कोई मॉडल प्रदाता कॉन्फ़िगर नहीं है।",
        dragHandleAria = "खींचने का हैंडल",
        editProviderAria = "प्रदाता संपादित करें",
        testProviderAria = "प्रदाता का परीक्षण",
        testProviderFailureAria = "प्रदाता परीक्षण विफल",
        testProviderSuccessAria = "प्रदाता परीक्षण सफल",
        deleteProviderAria = "प्रदाता हटाएँ",
        deleteProviderDialogTitle = "प्रदाता हटाएँ?",
        deleteProviderDialogText = { name -> "क्या आप वाकई \"$name\" को हटाना चाहते हैं? यह क्रिया वापस नहीं की जा सकती।" },
        deleteConfirm = "हटाएँ",
        cancel = "रद्द करें",
        nameLabel = "नाम",
        typeLabel = "प्रकार",
        removeProviderAria = "प्रदाता हटाएँ",
        ollamaBaseUrlLabel = "Ollama बेस URL",
        anthropicApiKeyLabel = "Anthropic API कुंजी",
        openAiApiKeyLabel = "OpenAI API कुंजी",
        googleApiKeyLabel = "Google API कुंजी",
        mistralAiApiKeyLabel = "Mistral AI API कुंजी",
        nameEmptyError = "नाम खाली नहीं हो सकता",
        nameUniqueError = "नाम अद्वितीय होना चाहिए",
        addProviderConfirmAria = "प्रदाता जोड़ें",
        modelLabel = "मॉडल",
        testAction = "परीक्षण",
        addAction = "जोड़ें",
        saveAction = "सहेजें",
        deleteAction = "हटाएँ",
        testFailedMessage = { msg -> "प्रदाता का परीक्षण विफल: $msg" },
    ),
    mcp = McpStrings(
        configuredServersTitle = "कॉन्फ़िगर किए गए MCP सर्वर",
        configuredServersDescription = "MCP सर्वरों को जोड़ें, हटाएँ और संपादित करें।",
        addServerAria = "सर्वर जोड़ें",
        noServersMessage = "कोई MCP सर्वर कॉन्फ़िगर नहीं है।",
        editServerAria = "सर्वर संपादित करें",
        deleteServerAria = "सर्वर हटाएँ",
        deleteServerDialogTitle = "सर्वर हटाएँ?",
        deleteServerDialogText = { name -> "क्या आप वाकई \"$name\" को हटाना चाहते हैं? यह क्रिया वापस नहीं की जा सकती।" },
        deleteConfirm = "हटाएँ",
        cancel = "रद्द करें",
        nameLabel = "नाम",
        transportLabel = "परिवहन",
        removeServerAria = "सर्वर हटाएँ",
        sseUrlLabel = "SSE URL",
        reconnectionTimeLabel = "पुन:कनेक्शन समय (सेकंड)",
        runAsProcessLabel = "प्रोसेस के रूप में चलाएँ",
        commandLabel = "कमांड (स्पेस से अलग)",
        environmentLabel = "पर्यावरण (key=value; key2=value2)",
        selectorLabel = "MCP सर्वर",
        selectorEmpty = "कोई MCP सर्वर उपलब्ध नहीं",
    ),
    prompts = PromptStrings(
        selectorLabel = "प्रॉम्प्ट",
    ),
    tools = ToolStrings(
        panelLabel = "उपकरण",
        emptyMessage = "कोई उपकरण उपलब्ध नहीं",
    ),
    conversations = ConversationStrings(
        untitledConversationTitle = "बिना शीर्षक वाला वार्तालाप",
        listTitle = "वार्तालाप",
        newConversationAria = "नया वार्तालाप",
        emptyTitle = "अभी तक कोई वार्तालाप नहीं",
        emptyBody = "यहाँ देखने के लिए नया वार्तालाप शुरू करें।",
        startNewButton = "नया वार्तालाप शुरू करें",
        openConversationAria = "वार्तालाप खोलें",
        conversationAria = "वार्तालाप",
        deleteConversationAria = "वार्तालाप हटाएँ",
        deleteDialogTitle = "वार्तालाप हटाएँ?",
        deleteDialogText = { title -> "क्या आप वाकई \"$title\" को हटाना चाहते हैं? यह क्रिया वापस नहीं की जा सकती।" },
        deleteConfirm = "हटाएँ",
        cancel = "रद्द करें",
        newConversationTitle = "नया वार्तालाप",
        createAria = "बनाएँ",
        qaNoModels = "कोई मॉडल उपलब्ध नहीं",
        qaNoToolModels = "टूल समर्थन वाले कोई मॉडल उपलब्ध नहीं",
        rpNoAvailableCharacters = "कोई चरित्र उपलब्ध नहीं",
        rpNoAvailablePersonas = "कोई व्यक्तित्व उपलब्ध नहीं",
        rpNoAvailableLorebooks = "कोई लोरबुक उपलब्ध नहीं",
        rpNoAvailableModel = "कोई मॉडल उपलब्ध नहीं",
        personaSettingsAria = "व्यक्तित्व सेटिंग्स",
        additionalLorebookLabel = "अतिरिक्त लोरबुक",
        newConversationIconAria = "नया वार्तालाप",
        inputPlaceholder = "संदेश लिखें",
        sendMessageAria = "संदेश भेजें",
        addAttachmentAria = "संलग्नक जोड़ें",
        newMessagesLabel = "नया",
        toolUseVettingTitle = "उपकरण उपयोग सत्यापन",
        agentWantsToCallToolPrefix = "एजेंट उपकरण कॉल करना चाहता है ",
        agentCalledToolPrefix = "एजेंट ने उपकरण कॉल किया ",
        detailsLabel = "विवरण",
        collapseAria = "समेटें",
        expandAria = "फैलाएँ",
        successAria = "सफलता",
        failureAria = "विफलता",
    ),
    appearance = AppearanceStrings(
        baseColorSchemeTitle = "मूल रंग योजना",
        baseColorSchemeDescription = "एप्लिकेशन में उपयोग की जाने वाली रंग योजना।",
        baseColorSchemeOptionDark = "डार्क",
        baseColorSchemeOptionLight = "लाइट",
        baseColorSchemeOptionSystem = "सिस्टम के अनुसार",
    ),
    developer = DeveloperStrings(
        openTelemetryTitle = "OpenTelemetry (gRPC)",
        openTelemetryDescription = "gRPC का उपयोग करके OpenTelemetry कलेक्टर को ट्रेस निर्यात करें।",
        openTelemetryEnabledLabel = "OpenTelemetry निर्यात सक्षम करें",
        openTelemetryEndpointLabel = "एंडपॉइंट",
        openTelemetryVerboseLabel = "विस्तृत निर्यात",
    ),
    settings = SettingsStrings(
        listTitle = "सेटिंग्स",
        selectSectionAria = "सेटिंग्स अनुभाग चुनें",
        sectionTitles = mapOf(
            "appearance" to "रूप-रंग",
            "mcp" to "MCP सर्वर",
            "models" to "मॉडल प्रदाता",
            "developer" to "डेवलपर",
        ),
    ),
    formats = FormatStrings(
        now = "अभी",
    ),
)
