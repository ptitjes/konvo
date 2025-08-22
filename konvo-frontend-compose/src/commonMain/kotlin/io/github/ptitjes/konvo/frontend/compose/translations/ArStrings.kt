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

@LyricistStrings(languageTag = "ar-SA")
internal val ArStrings = Strings(
    agents = AgentStrings(
        agentTypeDisplayName = {
            when (it) {
                AgentType.QuestionAnswer -> "سؤال وجواب"
                AgentType.Roleplay -> "تمثيل أدوار"
            }
        }
    ),
    models = ModelStrings(
        configuredProvidersTitle = "المزوّدون المُكوَّنون",
        configuredProvidersDescription = "إضافة وإزالة وتحرير مزوّدي النماذج.",
        addProviderAria = "إضافة مزوّد",
        noProvidersMessage = "لا يوجد مزوّدو نماذج مُكوَّنون.",
        dragHandleAria = "مقبض سحب",
        editProviderAria = "تحرير المزوّد",
        deleteProviderAria = "حذف المزوّد",
        deleteProviderDialogTitle = "حذف المزوّد؟",
        deleteProviderDialogText = { name -> "هل أنت متأكد من حذف \"$name\"؟ لا يمكن التراجع عن ذلك." },
        deleteConfirm = "حذف",
        cancel = "إلغاء",
        nameLabel = "الاسم",
        typeLabel = "النوع",
        removeProviderAria = "إزالة المزوّد",
        ollamaBaseUrlLabel = "عنوان Ollama الأساسي",
        anthropicApiKeyLabel = "مفتاح Anthropic API",
        openAiApiKeyLabel = "مفتاح OpenAI API",
        googleApiKeyLabel = "مفتاح Google API",
        nameEmptyError = "لا يمكن أن يكون الاسم فارغًا",
        nameUniqueError = "يجب أن يكون الاسم فريدًا",
        addProviderConfirmAria = "إضافة مزوّد",
        modelLabel = "النموذج",
    ),
    mcp = McpStrings(
        configuredServersTitle = "خوادم MCP المُكوَّنة",
        configuredServersDescription = "إضافة وإزالة وتحرير خوادم MCP.",
        addServerAria = "إضافة خادم",
        noServersMessage = "لا توجد خوادم MCP مُكوَّنة.",
        editServerAria = "تحرير الخادم",
        deleteServerAria = "حذف الخادم",
        deleteServerDialogTitle = "حذف الخادم؟",
        deleteServerDialogText = { name -> "هل أنت متأكد من حذف \"$name\"؟ لا يمكن التراجع عن ذلك." },
        deleteConfirm = "حذف",
        cancel = "إلغاء",
        nameLabel = "الاسم",
        transportLabel = "النقل",
        removeServerAria = "إزالة الخادم",
        sseUrlLabel = "عنوان SSE",
        reconnectionTimeLabel = "وقت إعادة الاتصال (بالثواني)",
        runAsProcessLabel = "تشغيل كعملية",
        commandLabel = "الأمر (مفصول بمسافات)",
        environmentLabel = "البيئة (key=value; key2=value2)",
        selectorLabel = "خوادم MCP",
        selectorEmpty = "لا توجد خوادم MCP متاحة",
    ),
    prompts = PromptStrings(
        selectorLabel = "المحفِّز",
    ),
    roleplay = RoleplayStrings(
        deleteConfirm = "حذف",
        cancel = "إلغاء",
        personaLabel = "الشخصية",
        personasTitle = "الشخصيات",
        personasDescription = "إضافة وإزالة وتحرير الشخصيات.",
        addPersonaAria = "إضافة شخصية",
        noPersonasConfigured = "لا توجد شخصيات مُكوَّنة.",
        nicknamePrefix = { nickname -> "الكنية: $nickname" },
        withLorebook = "مع كتاب الخلفية",
        editPersonaAria = "تحرير الشخصية",
        deletePersonaAria = "حذف الشخصية",
        deletePersonaDialogTitle = "حذف الشخصية؟",
        deletePersonaDialogText = { name -> "هل أنت متأكد من حذف \"$name\"؟ لا يمكن التراجع عن ذلك." },
        nameLabel = "الاسم",
        nicknameLabel = "الكنية",
        defaultLorebookLabel = "كتاب الخلفية الافتراضي",
        saveAction = "حفظ",
        addAction = "إضافة",
        removePersonaAria = "إزالة الشخصية",
        lorebookLabel = "كتاب الخلفية",
        lorebookNone = "لا شيء",
        lorebookUnnamed = "كتاب خلفية بلا اسم",
        importedLorebooksTitle = "كتب الخلفية المستوردة",
        importedLorebooksDescription = "استيراد وسرد وحذف كتب الخلفية.",
        importLorebookAria = "استيراد كتاب خلفية",
        failedToLoadLorebooks = { msg -> "فشل تحميل كتب الخلفية: $msg" },
        noLorebooksAvailable = "لا توجد كتب خلفية متاحة.",
        deleteLorebookDialogTitle = "حذف كتاب الخلفية؟",
        deleteLorebookDialogText = { name -> "هل أنت متأكد من حذف \"$name\"؟ لا يمكن التراجع عن ذلك." },
        deleteLorebookAria = "حذف كتاب الخلفية",
        characterTagsFilterTitle = "تصفية وسوم الشخصيات",
        characterTagsFilterDescription = "سيتم استبعاد الوسوم المُدرجة هنا عند عرض الشخصيات. افصل الوسوم بفواصل.",
        characterTagsPlaceholder = "مثال: nsfw, beta, wip",
        importedCharactersTitle = "الشخصيات المستوردة",
        importedCharactersDescription = "استيراد وسرد وحذف الشخصيات.",
        importCharactersAria = "استيراد شخصيات",
        failedToLoadCharacters = { msg -> "فشل تحميل الشخصيات: $msg" },
        noCharactersAvailable = "لا توجد شخصيات متاحة.",
        deleteCharacterDialogTitle = "حذف الشخصية؟",
        deleteCharacterDialogText = { name -> "هل أنت متأكد من حذف \"$name\"؟ لا يمكن التراجع عن ذلك." },
        deleteCharacterAria = "حذف الشخصية",
        hasCharacterBookAria = "يحتوي على كتاب شخصية",
        defaultPersonaTitle = "الشخصية الافتراضية",
        defaultPersonaDescription = "تُستخدم كشخصيتك في محادثات تمثيل الأدوار الجديدة.",
        noPersonaDefined = "لا توجد شخصية معرَّفة بعد",
        defaultPreferredModelTitle = "النموذج المفضل الافتراضي",
        defaultPreferredModelDescription = "النموذج المستخدم افتراضيًا في محادثات تمثيل الأدوار الجديدة.",
        noAvailableModels = "لا توجد نماذج متاحة",
        defaultSystemPromptTitle = "المحفِّز النظامي الافتراضي",
        defaultSystemPromptDescription = "يُستخدم عندما لا تُعرِّف بطاقة الشخصية محفِّزها النظامي الخاص.",
        defaultLorebookSettingsTitle = "إعدادات كتاب الخلفية الافتراضية",
        defaultLorebookSettingsDescription = "تُستخدم عندما لا تُعرِّف بطاقة الشخصية إعدادات كتاب الخلفية الخاصة بها.",
        scanDepthLabel = "عمق المسح",
        tokenBudgetLabel = "ميزانية الرموز",
        recursiveScanningLabel = "مسح تكراري",
        greetingLabel = "تحية",
        randomGreeting = "تحية عشوائية",
        greetingOptionLabel = { index, preview -> "تحية ${index + 1}: $preview" },
    ),
    tools = ToolStrings(
        panelLabel = "الأدوات",
        emptyMessage = "لا توجد أدوات متاحة",
    ),
    conversations = ConversationStrings(
        backAria = "رجوع",
        settingsAria = "الإعدادات",
        listTitle = "المحادثات",
        newConversationAria = "محادثة جديدة",
        emptyTitle = "لا توجد محادثات بعد",
        emptyBody = "ابدأ محادثة جديدة لتظهر هنا.",
        startNewButton = "ابدأ محادثة جديدة",
        openConversationAria = "فتح المحادثة",
        conversationAria = "محادثة",
        deleteConversationAria = "حذف المحادثة",
        deleteDialogTitle = "حذف المحادثة؟",
        deleteDialogText = { title -> "هل أنت متأكد من حذف \"$title\"؟ لا يمكن التراجع عن ذلك." },
        deleteConfirm = "حذف",
        cancel = "إلغاء",
        newConversationTitle = "محادثة جديدة",
        createAria = "إنشاء",
        qaNoModels = "لا توجد نماذج متاحة",
        qaNoToolModels = "لا توجد نماذج متاحة بدعم الأدوات",
        rpNoCharactersOrModels = "لا توجد شخصيات أو نماذج متاحة",
        personaSettingsAria = "إعدادات الشخصية",
        additionalLorebookLabel = "كتاب خلفية إضافي",
        newConversationIconAria = "محادثة جديدة",
        inputPlaceholder = "اكتب رسالة",
        sendMessageAria = "إرسال الرسالة",
        addAttachmentAria = "إضافة مرفق",
        newMessagesLabel = "جديد",
        toolUseVettingTitle = "مراجعة استخدام الأدوات",
        agentCalledToolPrefix = "الوكيل استدعى أداة: ",
        detailsLabel = "تفاصيل",
        collapseAria = "طي",
        expandAria = "توسيع",
        successAria = "نجاح",
        failureAria = "فشل",
    ),
    settings = SettingsStrings(
        listTitle = "الإعدادات",
        selectSectionAria = "اختيار قسم الإعدادات",
        sectionTitles = mapOf(
            "appearance" to "المظهر",
            "mcp" to "خوادم MCP",
            "models" to "مزوّدو النماذج",
            "roleplay" to "تمثيل الأدوار",
            "characters" to "الشخصيات",
            "lorebooks" to "كتب الخلفية",
            "personas" to "الشخصيات",
        ),
        appearanceBaseColorSchemeTitle = "نظام الألوان الأساسي",
        appearanceBaseColorSchemeDescription = "نظام الألوان المستخدم للتطبيق.",
        appearanceBaseColorSchemeOptionDark = "داكن",
        appearanceBaseColorSchemeOptionLight = "فاتح",
        appearanceBaseColorSchemeOptionSystem = "متوافق مع النظام",
    ),
    navigationDestinationTitles = { state ->
        when (state) {
            AppState.Conversations -> "المحادثات"
            AppState.Archive -> "الأرشيف"
            AppState.KnowledgeBases -> "قواعد المعرفة"
            AppState.Settings -> "الإعدادات"
        }
    },
    formats = FormatStrings(
        now = "الآن",
    ),
)
