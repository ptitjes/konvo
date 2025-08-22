package io.github.ptitjes.konvo.frontend.compose.translations

import cafe.adriel.lyricist.*
import io.github.ptitjes.konvo.frontend.compose.agents.*
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
    )
)
