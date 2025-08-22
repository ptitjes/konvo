package io.github.ptitjes.konvo.frontend.compose.translations

import cafe.adriel.lyricist.*
import io.github.ptitjes.konvo.frontend.compose.agents.*
import io.github.ptitjes.konvo.frontend.compose.mcp.*
import io.github.ptitjes.konvo.frontend.compose.models.*

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
)
