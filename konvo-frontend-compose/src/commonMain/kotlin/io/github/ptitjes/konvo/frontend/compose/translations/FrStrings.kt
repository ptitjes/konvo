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

@LyricistStrings(languageTag = "fr-FR", default = true)
internal val FrStrings = Strings(
    agents = AgentStrings(
        agentTypeDisplayName = {
            when (it) {
                AgentType.QuestionAnswer -> "Question/Réponse"
                AgentType.Roleplay -> "Jeux de rôle"
            }
        }
    ),
    models = ModelStrings(
        configuredProvidersTitle = "Fournisseurs configurés",
        configuredProvidersDescription = "Ajouter, supprimer et modifier des fournisseurs de modèles.",
        addProviderAria = "Ajouter un fournisseur",
        noProvidersMessage = "Aucun fournisseur de modèles configuré.",
        dragHandleAria = "Poignée de déplacement",
        editProviderAria = "Modifier le fournisseur",
        deleteProviderAria = "Supprimer le fournisseur",
        deleteProviderDialogTitle = "Supprimer le fournisseur ?",
        deleteProviderDialogText = { name -> "Êtes-vous sûr de vouloir supprimer \"$name\" ? Cette action est irréversible." },
        deleteConfirm = "Supprimer",
        cancel = "Annuler",
        nameLabel = "Nom",
        typeLabel = "Type",
        removeProviderAria = "Retirer le fournisseur",
        ollamaBaseUrlLabel = "URL de base Ollama",
        anthropicApiKeyLabel = "Clé API Anthropic",
        openAiApiKeyLabel = "Clé API OpenAI",
        googleApiKeyLabel = "Clé API Google",
        nameEmptyError = "Le nom ne peut pas être vide",
        nameUniqueError = "Le nom doit être unique",
        addProviderConfirmAria = "Ajouter un fournisseur",
        modelLabel = "Modèle",
    ),
    mcp = McpStrings(
        configuredServersTitle = "Serveurs MCP configurés",
        configuredServersDescription = "Ajouter, supprimer et modifier des serveurs MCP.",
        addServerAria = "Ajouter un serveur",
        noServersMessage = "Aucun serveur MCP configuré.",
        editServerAria = "Modifier le serveur",
        deleteServerAria = "Supprimer le serveur",
        deleteServerDialogTitle = "Supprimer le serveur ?",
        deleteServerDialogText = { name -> "Êtes-vous sûr de vouloir supprimer \"$name\" ? Cette action est irréversible." },
        deleteConfirm = "Supprimer",
        cancel = "Annuler",
        nameLabel = "Nom",
        transportLabel = "Transport",
        removeServerAria = "Retirer le serveur",
        sseUrlLabel = "URL SSE",
        reconnectionTimeLabel = "Temps de reconnexion (secondes)",
        runAsProcessLabel = "Exécuter en tant que processus",
        commandLabel = "Commande (séparée par des espaces)",
        environmentLabel = "Environnement (clé=valeur; clé2=valeur2)",
        selectorLabel = "Serveurs MCP",
        selectorEmpty = "Aucun serveur MCP disponible",
    ),
    prompts = PromptStrings(
        selectorLabel = "Invite",
    ),
    roleplay = RoleplayStrings(
        deleteConfirm = "Supprimer",
        cancel = "Annuler",
        personaLabel = "Personnage",
        personasTitle = "Personnages",
        personasDescription = "Ajouter, supprimer et modifier des personnages.",
        addPersonaAria = "Ajouter un personnage",
        noPersonasConfigured = "Aucun personnage configuré.",
        nicknamePrefix = { nickname -> "Surnom : $nickname" },
        withLorebook = "Avec lorebook",
        editPersonaAria = "Modifier le personnage",
        deletePersonaAria = "Supprimer le personnage",
        deletePersonaDialogTitle = "Supprimer le personnage ?",
        deletePersonaDialogText = { name -> "Êtes-vous sûr de vouloir supprimer \"$name\" ? Cette action est irréversible." },
        nameLabel = "Nom",
        nicknameLabel = "Surnom",
        defaultLorebookLabel = "Lorebook par défaut",
        saveAction = "Enregistrer",
        addAction = "Ajouter",
        removePersonaAria = "Retirer le personnage",
        lorebookLabel = "Lorebook",
        lorebookNone = "Aucun",
        lorebookUnnamed = "Lorebook sans nom",
        importedLorebooksTitle = "Lorebooks importés",
        importedLorebooksDescription = "Importer, lister et supprimer des lorebooks.",
        importLorebookAria = "Importer un lorebook",
        failedToLoadLorebooks = { msg -> "Échec du chargement des lorebooks : $msg" },
        noLorebooksAvailable = "Aucun lorebook disponible.",
        deleteLorebookDialogTitle = "Supprimer le lorebook ?",
        deleteLorebookDialogText = { name -> "Êtes-vous sûr de vouloir supprimer \"$name\" ? Cette action est irréversible." },
        deleteLorebookAria = "Supprimer le lorebook",
        characterTagsFilterTitle = "Filtre des tags de personnages",
        characterTagsFilterDescription = "Les tags listés ici seront exclus lors de l'affichage des personnages. Séparez les tags par des virgules.",
        characterTagsPlaceholder = "ex. nsfw, beta, wip",
        importedCharactersTitle = "Personnages importés",
        importedCharactersDescription = "Importer, lister et supprimer des personnages.",
        importCharactersAria = "Importer des personnages",
        failedToLoadCharacters = { msg -> "Échec du chargement des personnages : $msg" },
        noCharactersAvailable = "Aucun personnage disponible.",
        deleteCharacterDialogTitle = "Supprimer le personnage ?",
        deleteCharacterDialogText = { name -> "Êtes-vous sûr de vouloir supprimer \"$name\" ? Cette action est irréversible." },
        deleteCharacterAria = "Supprimer le personnage",
        hasCharacterBookAria = "Possède un livre de personnage",
        defaultPersonaTitle = "Personnage par défaut",
        defaultPersonaDescription = "Utilisé comme votre personnage dans les nouvelles conversations de jeu de rôle.",
        noPersonaDefined = "Aucun personnage défini pour l'instant",
        defaultPreferredModelTitle = "Modèle préféré par défaut",
        defaultPreferredModelDescription = "Modèle utilisé par défaut pour les nouvelles conversations de jeu de rôle.",
        noAvailableModels = "Aucun modèle disponible",
        defaultSystemPromptTitle = "Invite système par défaut",
        defaultSystemPromptDescription = "Utilisée lorsque la fiche de personnage ne définit pas sa propre invite système.",
        defaultLorebookSettingsTitle = "Paramètres de Lorebook par défaut",
        defaultLorebookSettingsDescription = "Utilisés lorsque la fiche de personnage ne définit pas sa propre configuration de lorebook.",
        scanDepthLabel = "Profondeur d'analyse",
        tokenBudgetLabel = "Budget de tokens",
        recursiveScanningLabel = "Analyse récursive",
        greetingLabel = "Salutation",
        randomGreeting = "Salutation aléatoire",
        greetingOptionLabel = { index, preview -> "Salutation ${index + 1} : $preview" },
    ),
    tools = ToolStrings(
        panelLabel = "Outils",
        emptyMessage = "Aucun outil disponible",
    ),
    conversations = ConversationStrings(
        backAria = "Retour",
        settingsAria = "Paramètres",
        listTitle = "Conversations",
        newConversationAria = "Nouvelle conversation",
        emptyTitle = "Aucune conversation pour l'instant",
        emptyBody = "Démarrez une nouvelle conversation pour la voir ici.",
        startNewButton = "Démarrer une nouvelle conversation",
        openConversationAria = "Ouvrir la conversation",
        conversationAria = "Conversation",
        deleteConversationAria = "Supprimer la conversation",
        deleteDialogTitle = "Supprimer la conversation ?",
        deleteDialogText = { title -> "Êtes-vous sûr de vouloir supprimer \"$title\"Cette action est irréversible." },
        deleteConfirm = "Supprimer",
        cancel = "Annuler",
        newConversationTitle = "Nouvelle conversation",
        createAria = "Créer",
        qaNoModels = "Aucun modèle disponible",
        qaNoToolModels = "Aucun modèle disponible avec support des outils",
        rpNoCharactersOrModels = "Aucun personnage ou modèle disponible",
        personaSettingsAria = "Paramètres du personnage",
        additionalLorebookLabel = "Lorebook supplémentaire",
        newConversationIconAria = "Nouvelle conversation",
        inputPlaceholder = "Écrire un message",
        sendMessageAria = "Envoyer le message",
        addAttachmentAria = "Ajouter une pièce jointe",
        newMessagesLabel = "Nouveaux",
        toolUseVettingTitle = "Validation d'utilisation d'outil",
        agentCalledToolPrefix = "L'agent a appelé l'outil : ",
        detailsLabel = "Détails",
        collapseAria = "Replier",
        expandAria = "Déplier",
        successAria = "Succès",
        failureAria = "Échec",
    ),
    settings = SettingsStrings(
        listTitle = "Paramètres",
        selectSectionAria = "Sélectionner une section des paramètres",
        sectionTitles = mapOf(
            "appearance" to "Apparence",
            "mcp" to "Serveurs MCP",
            "models" to "Fournisseurs de modèles",
            "roleplay" to "Jeux de rôle",
            "characters" to "Personnages",
            "lorebooks" to "Lorebooks",
            "personas" to "Personnages",
        ),
        appearanceBaseColorSchemeTitle = "Schéma de couleurs de base",
        appearanceBaseColorSchemeDescription = "Le schéma de couleurs utilisé pour l'application.",
        appearanceBaseColorSchemeOptionDark = "Sombre",
        appearanceBaseColorSchemeOptionLight = "Clair",
        appearanceBaseColorSchemeOptionSystem = "Adapté au système",
    ),
    navigationDestinationTitles = { state ->
        when (state) {
            AppState.Conversations -> "Conversations"
            AppState.Archive -> "Archives"
            AppState.KnowledgeBases -> "Bases de connaissances"
            AppState.Settings -> "Paramètres"
        }
    },
    formats = FormatStrings(
        now = "maintenant",
    ),
)
