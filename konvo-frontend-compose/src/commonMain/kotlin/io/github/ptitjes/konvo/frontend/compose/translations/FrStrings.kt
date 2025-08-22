package io.github.ptitjes.konvo.frontend.compose.translations

import cafe.adriel.lyricist.*
import io.github.ptitjes.konvo.frontend.compose.agents.*

@LyricistStrings(languageTag = "fr-FR", default = true)
internal val FrStrings = Strings(
    agents = AgentStrings(
        agentTypeDisplayName = {
            when (it) {
                AgentType.QuestionAnswer -> "Question/Réponse"
                AgentType.Roleplay -> "Jeux de rôle"
            }
        }
    )
)
