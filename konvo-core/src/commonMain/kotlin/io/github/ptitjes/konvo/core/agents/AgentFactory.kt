package io.github.ptitjes.konvo.core.agents

import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.settings.*
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
                QuestionAnswerAgent(
                    settingsRepository = settingsRepository,
                    modelProviderManager = modelProviderManager,
                    mcpSessionFactory = mcpSessionFactory,
                    configuration = agentConfiguration,
                )
            }

            is RoleplayAgentConfiguration -> {
                RoleplayAgent(
                    modelProviderManager = modelProviderManager,
                    characterProviderManager = characterProviderManager,
                    settingsRepository = settingsRepository,
                    lorebookManager = lorebookManager,
                    configuration = agentConfiguration,
                )
            }

            is NoAgentConfiguration -> error("No agent configured for this conversation")
        }
    }
}
