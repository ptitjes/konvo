package io.github.ptitjes.konvo.core.agents

import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import kotlin.coroutines.*

class AgentFactory(
    private val modelProviderManager: ModelManager,
    private val mcpSessionFactory: (coroutineContext: CoroutineContext) -> McpHostSession,
    private val characterProviderManager: CharacterManager,
) {

    suspend fun createAgent(agentConfiguration: AgentConfiguration): Agent {
        return when (agentConfiguration) {
            is QuestionAnswerAgentConfiguration -> buildQuestionAnswerAgent(
                model = modelProviderManager.named(agentConfiguration.modelName),
                mcpSessionFactory = mcpSessionFactory,
                toolNames = agentConfiguration.toolNames,
            )

            is RoleplayAgentConfiguration -> buildRoleplayAgent(
                model = modelProviderManager.named(agentConfiguration.modelName),
                character = characterProviderManager.withId(agentConfiguration.characterId),
                characterGreetingIndex = agentConfiguration.characterGreetingIndex,
                userName = agentConfiguration.userName,
            )

            is NoAgentConfiguration -> error("No agent configured for this conversation")
        }
    }
}
