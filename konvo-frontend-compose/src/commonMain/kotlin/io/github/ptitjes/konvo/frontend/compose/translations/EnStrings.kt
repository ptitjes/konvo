package io.github.ptitjes.konvo.frontend.compose.translations

import cafe.adriel.lyricist.*
import io.github.ptitjes.konvo.frontend.compose.agents.*

@LyricistStrings(languageTag = "en-US", default = true)
internal val EnStrings = Strings(
    agents = AgentStrings(
        agentTypeDisplayName = {
            when (it) {
                AgentType.QuestionAnswer -> "Question & Answer"
                AgentType.Roleplay -> "Role-play"
            }
        }
    )
)
