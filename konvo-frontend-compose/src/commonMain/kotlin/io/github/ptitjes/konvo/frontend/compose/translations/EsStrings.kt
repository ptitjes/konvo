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

@LyricistStrings(languageTag = "es-ES")
internal val EsStrings = Strings(
    agents = AgentStrings(
        agentTypeDisplayName = {
            when (it) {
                AgentType.QuestionAnswer -> "Preguntas y respuestas"
                AgentType.Roleplay -> "Juego de rol"
            }
        }
    ),
    models = ModelStrings(
        configuredProvidersTitle = "Proveedores configurados",
        configuredProvidersDescription = "Añadir, eliminar y editar proveedores de modelos.",
        addProviderAria = "Añadir proveedor",
        noProvidersMessage = "No hay proveedores de modelos configurados.",
        dragHandleAria = "Asa de arrastre",
        editProviderAria = "Editar proveedor",
        deleteProviderAria = "Eliminar proveedor",
        deleteProviderDialogTitle = "¿Eliminar proveedor?",
        deleteProviderDialogText = { name -> "¿Seguro que quieres eliminar \"$name\"? Esta acción no se puede deshacer." },
        deleteConfirm = "Eliminar",
        cancel = "Cancelar",
        nameLabel = "Nombre",
        typeLabel = "Tipo",
        removeProviderAria = "Quitar proveedor",
        ollamaBaseUrlLabel = "URL base de Ollama",
        anthropicApiKeyLabel = "Clave API de Anthropic",
        openAiApiKeyLabel = "Clave API de OpenAI",
        googleApiKeyLabel = "Clave API de Google",
        nameEmptyError = "El nombre no puede estar vacío",
        nameUniqueError = "El nombre debe ser único",
        addProviderConfirmAria = "Añadir proveedor",
        modelLabel = "Modelo",
    ),
    mcp = McpStrings(
        configuredServersTitle = "Servidores MCP configurados",
        configuredServersDescription = "Añadir, eliminar y editar servidores MCP.",
        addServerAria = "Añadir servidor",
        noServersMessage = "No hay servidores MCP configurados.",
        editServerAria = "Editar servidor",
        deleteServerAria = "Eliminar servidor",
        deleteServerDialogTitle = "¿Eliminar servidor?",
        deleteServerDialogText = { name -> "¿Seguro que quieres eliminar \"$name\"? Esta acción no se puede deshacer." },
        deleteConfirm = "Eliminar",
        cancel = "Cancelar",
        nameLabel = "Nombre",
        transportLabel = "Transporte",
        removeServerAria = "Quitar servidor",
        sseUrlLabel = "URL SSE",
        reconnectionTimeLabel = "Tiempo de reconexión (segundos)",
        runAsProcessLabel = "Ejecutar como proceso",
        commandLabel = "Comando (separado por espacios)",
        environmentLabel = "Entorno (clave=valor; clave2=valor2)",
        selectorLabel = "Servidores MCP",
        selectorEmpty = "No hay servidores MCP disponibles",
    ),
    prompts = PromptStrings(
        selectorLabel = "Prompt",
    ),
    roleplay = RoleplayStrings(
        deleteConfirm = "Eliminar",
        cancel = "Cancelar",
        personaLabel = "Persona",
        personasTitle = "Personas",
        personasDescription = "Añadir, eliminar y editar personas.",
        addPersonaAria = "Añadir persona",
        noPersonasConfigured = "No hay personas configuradas.",
        nicknamePrefix = { nickname -> "Apodo: $nickname" },
        withLorebook = "Con libro de lore",
        editPersonaAria = "Editar persona",
        deletePersonaAria = "Eliminar persona",
        deletePersonaDialogTitle = "¿Eliminar persona?",
        deletePersonaDialogText = { name -> "¿Seguro que quieres eliminar \"$name\"? Esta acción no se puede deshacer." },
        nameLabel = "Nombre",
        nicknameLabel = "Apodo",
        defaultLorebookLabel = "Libro de lore por defecto",
        saveAction = "Guardar",
        addAction = "Añadir",
        removePersonaAria = "Quitar persona",
        lorebookLabel = "Libro de lore",
        lorebookNone = "Ninguno",
        lorebookUnnamed = "Libro de lore sin nombre",
        importedLorebooksTitle = "Libros de lore importados",
        importedLorebooksDescription = "Importar, listar y eliminar libros de lore.",
        importLorebookAria = "Importar libro de lore",
        failedToLoadLorebooks = { msg -> "Error al cargar los libros de lore: $msg" },
        noLorebooksAvailable = "No hay libros de lore disponibles.",
        deleteLorebookDialogTitle = "¿Eliminar libro de lore?",
        deleteLorebookDialogText = { name -> "¿Seguro que quieres eliminar \"$name\"? Esta acción no se puede deshacer." },
        deleteLorebookAria = "Eliminar libro de lore",
        characterTagsFilterTitle = "Filtro de etiquetas de personaje",
        characterTagsFilterDescription = "Las etiquetas listadas aquí se excluirán al mostrar personajes. Separe las etiquetas con comas.",
        characterTagsPlaceholder = "p. ej., nsfw, beta, wip",
        importedCharactersTitle = "Personajes importados",
        importedCharactersDescription = "Importar, listar y eliminar personajes.",
        importCharactersAria = "Importar personajes",
        failedToLoadCharacters = { msg -> "Error al cargar los personajes: $msg" },
        noCharactersAvailable = "No hay personajes disponibles.",
        deleteCharacterDialogTitle = "¿Eliminar personaje?",
        deleteCharacterDialogText = { name -> "¿Seguro que quieres eliminar \"$name\"? Esta acción no se puede deshacer." },
        deleteCharacterAria = "Eliminar personaje",
        hasCharacterBookAria = "Tiene libro de personaje",
        defaultPersonaTitle = "Persona por defecto",
        defaultPersonaDescription = "Se usa como tu persona en nuevas conversaciones de rol.",
        noPersonaDefined = "Aún no se ha definido ninguna persona",
        defaultPreferredModelTitle = "Modelo preferido por defecto",
        defaultPreferredModelDescription = "Modelo usado por defecto para nuevas conversaciones de rol.",
        noAvailableModels = "No hay modelos disponibles",
        defaultSystemPromptTitle = "Prompt del sistema por defecto",
        defaultSystemPromptDescription = "Se usa cuando la ficha del personaje no define su propio prompt del sistema.",
        defaultLorebookSettingsTitle = "Ajustes del libro de lore por defecto",
        defaultLorebookSettingsDescription = "Se usa cuando la ficha del personaje no define su propia configuración del libro de lore.",
        scanDepthLabel = "Profundidad de exploración",
        tokenBudgetLabel = "Presupuesto de tokens",
        recursiveScanningLabel = "Exploración recursiva",
        greetingLabel = "Saludo",
        randomGreeting = "Saludo aleatorio",
        greetingOptionLabel = { index, preview -> "Saludo ${index + 1}: $preview" },
    ),
    tools = ToolStrings(
        panelLabel = "Herramientas",
        emptyMessage = "No hay herramientas disponibles",
    ),
    conversations = ConversationStrings(
        backAria = "Atrás",
        settingsAria = "Ajustes",
        listTitle = "Conversaciones",
        newConversationAria = "Nueva conversación",
        emptyTitle = "Aún no hay conversaciones",
        emptyBody = "Inicia una nueva conversación para verla aquí.",
        startNewButton = "Iniciar nueva conversación",
        openConversationAria = "Abrir conversación",
        conversationAria = "Conversación",
        deleteConversationAria = "Eliminar conversación",
        deleteDialogTitle = "¿Eliminar conversación?",
        deleteDialogText = { title -> "¿Seguro que quieres eliminar \"$title\"? Esta acción no se puede deshacer." },
        deleteConfirm = "Eliminar",
        cancel = "Cancelar",
        newConversationTitle = "Nueva conversación",
        createAria = "Crear",
        qaNoModels = "No hay modelos disponibles",
        qaNoToolModels = "No hay modelos disponibles con soporte de herramientas",
        rpNoCharactersOrModels = "No hay personajes o modelos disponibles",
        personaSettingsAria = "Ajustes de la persona",
        additionalLorebookLabel = "Libro de lore adicional",
        newConversationIconAria = "Nueva conversación",
        inputPlaceholder = "Escribe un mensaje",
        sendMessageAria = "Enviar el mensaje",
        addAttachmentAria = "Añadir un adjunto",
        newMessagesLabel = "Nuevo",
        toolUseVettingTitle = "Revisión de uso de herramientas",
        agentCalledToolPrefix = "El agente llamó a la herramienta: ",
        detailsLabel = "Detalles",
        collapseAria = "Contraer",
        expandAria = "Expandir",
        successAria = "Éxito",
        failureAria = "Fallo",
    ),
    settings = SettingsStrings(
        listTitle = "Ajustes",
        selectSectionAria = "Seleccionar sección de ajustes",
        sectionTitles = mapOf(
            "appearance" to "Apariencia",
            "mcp" to "Servidores MCP",
            "models" to "Proveedores de modelos",
            "roleplay" to "Juego de rol",
            "characters" to "Personajes",
            "lorebooks" to "Libros de lore",
            "personas" to "Personas",
        ),
        appearanceBaseColorSchemeTitle = "Esquema de colores base",
        appearanceBaseColorSchemeDescription = "El esquema de colores utilizado por la aplicación.",
        appearanceBaseColorSchemeOptionDark = "Oscuro",
        appearanceBaseColorSchemeOptionLight = "Claro",
        appearanceBaseColorSchemeOptionSystem = "Adaptar al sistema",
    ),
    navigationDestinationTitles = { state ->
        when (state) {
            AppState.Conversations -> "Conversaciones"
            AppState.Archive -> "Archivo"
            AppState.KnowledgeBases -> "Bases de conocimiento"
            AppState.Settings -> "Ajustes"
        }
    },
    formats = FormatStrings(
        now = "ahora",
    ),
)
