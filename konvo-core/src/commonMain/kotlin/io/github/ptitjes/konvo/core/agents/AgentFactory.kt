package io.github.ptitjes.konvo.core.agents

import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.settings.*
import kotlinx.coroutines.flow.*
import kotlin.coroutines.*

class AgentFactory(
    private val modelProviderManager: ModelManager,
    private val mcpSessionFactory: (coroutineContext: CoroutineContext) -> McpHostSession,
    private val characterProviderManager: CharacterManager,
    private val settingsRepository: SettingsRepository,
    private val lorebookManager: LorebookManager,
) {

    suspend fun createAgent(agentConfiguration: AgentConfiguration): Agent {
        return when (agentConfiguration) {
            is QuestionAnswerAgentConfiguration -> {
                val model = modelProviderManager.named(agentConfiguration.modelName)

                buildQuestionAnswerAgent(
                    model = model,
                    mcpSessionFactory = mcpSessionFactory,
                    mcpServerNames = agentConfiguration.mcpServerNames,
                )
            }

            is RoleplayAgentConfiguration -> {
                val roleplaySettings = settingsRepository.getSettings(RoleplaySettingsKey).first()
                val model = modelProviderManager.named(agentConfiguration.modelName)
                val character = characterProviderManager.withId(agentConfiguration.characterId)
                val personaSettings = settingsRepository.getSettings(PersonaSettingsKey).first()
                val persona = personaSettings.personas.first { it.name == agentConfiguration.personaName }
                val lorebook = agentConfiguration.lorebookId?.let { id -> lorebookManager.withId(id) }

                buildRoleplayAgent(
                    roleplaySettings = roleplaySettings,
                    roleplayConfiguration = agentConfiguration,
                    model = model,
                    character = character,
                    persona = persona,
                    lorebook = lorebook,
                )
            }

            is NoAgentConfiguration -> error("No agent configured for this conversation")
        }
    }
}
