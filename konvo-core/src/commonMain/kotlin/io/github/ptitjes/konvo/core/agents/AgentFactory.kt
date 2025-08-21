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
            is QuestionAnswerAgentConfiguration -> buildQuestionAnswerAgent(
                model = modelProviderManager.named(agentConfiguration.modelName),
                mcpSessionFactory = mcpSessionFactory,
                mcpServerNames = agentConfiguration.mcpServerNames,
            )

            is RoleplayAgentConfiguration -> buildRoleplayAgent(
                roleplayAgentSettings = settingsRepository.getSettings(RoleplayAgentSettingsKey).first(),
                roleplayConfiguration = agentConfiguration,
                model = modelProviderManager.named(agentConfiguration.modelName),
                character = characterProviderManager.withId(agentConfiguration.characterId),
                lorebook = agentConfiguration.lorebookId?.let { id -> lorebookManager.withId(id) },
            )

            is NoAgentConfiguration -> error("No agent configured for this conversation")
        }
    }
}
