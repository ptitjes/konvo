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

internal val EsStrings = Strings(
    navigation = NavigationStrings(
        navigationOpenAria = "Abrir navegación",
        navigationCloseAria = "Cerrar navegación",
        backAria = "Atrás",
        detailsOpenAria = "Abrir detalles",
        detailsCloseAria = "Cerrar detalles",
    ),
    agents = AgentStrings(
        agentTypeDisplayName = {
            when (it) {
                AgentType.QuestionAnswer -> "Preguntas y respuestas"
                AgentType.Roleplay -> "Juego de rol"
            }
        }
    ),
    models = ModelStrings(
        settingsTitle = "Proveedores de modelos",
        configuredProvidersTitle = "Proveedores configurados",
        configuredProvidersDescription = "Añadir, eliminar y editar proveedores de modelos.",
        addProviderAria = "Añadir proveedor",
        noProvidersMessage = "No hay proveedores de modelos configurados.",
        dragHandleAria = "Asa de arrastre",
        editProviderAria = "Editar proveedor",
        testProviderAria = "Probar proveedor",
        testProviderFailureAria = "Error al probar el proveedor",
        testProviderSuccessAria = "Prueba del proveedor exitosa",
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
        mistralAiApiKeyLabel = "Clave API de Mistral AI",
        nameEmptyError = "El nombre no puede estar vacío",
        nameUniqueError = "El nombre debe ser único",
        addProviderConfirmAria = "Añadir proveedor",
        modelLabel = "Modelo",
        testAction = "Probar",
        addAction = "Añadir",
        saveAction = "Guardar",
        deleteAction = "Eliminar",
        testFailedMessage = { msg -> "Error al probar el proveedor: $msg" },
    ),
    mcp = McpStrings(
        settingsTitle = "Servidores MCP",
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
    tools = ToolStrings(
        panelLabel = "Herramientas",
        emptyMessage = "No hay herramientas disponibles",
    ),
    conversations = ConversationStrings(
        untitledConversationTitle = "Conversación sin título",
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
        rpNoAvailableCharacters = "No hay personajes disponibles",
        rpNoAvailablePersonas = "No hay avatares disponibles",
        rpNoAvailableLorebooks = "No hay libros de lore disponibles",
        rpNoAvailableModel = "No hay modelos disponibles",
        personaSettingsAria = "Ajustes del avatar",
        additionalLorebookLabel = "Libro de lore adicional",
        newConversationIconAria = "Nueva conversación",
        inputPlaceholder = "Escribe un mensaje",
        sendMessageAria = "Enviar el mensaje",
        addAttachmentAria = "Añadir un adjunto",
        newMessagesLabel = "Nuevo",
        toolUseVettingTitle = "Revisión de uso de herramientas",
        agentWantsToCallToolPrefix = "El agente quiere llamar a la herramienta ",
        agentCalledToolPrefix = "El agente llamó a la herramienta ",
        detailsLabel = "Detalles",
        collapseAria = "Contraer",
        expandAria = "Expandir",
        successAria = "Éxito",
        failureAria = "Fallo",
    ),
    appearance = AppearanceStrings(
        settingsTitle = "Apariencia",
        baseColorSchemeTitle = "Esquema de colores base",
        baseColorSchemeDescription = "El esquema de colores utilizado por la aplicación.",
        baseColorSchemeOptionDark = "Oscuro",
        baseColorSchemeOptionLight = "Claro",
        baseColorSchemeOptionSystem = "Adaptar al sistema",
    ),
    developer = DeveloperStrings(
        settingsTitle = "Desarrollador",
        openTelemetryTitle = "OpenTelemetry (gRPC)",
        openTelemetryDescription = "Exportar trazas a un recolector de OpenTelemetry mediante gRPC.",
        openTelemetryEnabledLabel = "Activar exportación de OpenTelemetry",
        openTelemetryEndpointLabel = "Punto final",
        openTelemetryVerboseLabel = "Exportación detallada",
    ),
    settings = SettingsStrings(
        listTitle = "Ajustes",
        selectSectionAria = "Seleccionar sección de ajustes",
    ),
    formats = FormatStrings(
        now = "ahora",
    ),
)
