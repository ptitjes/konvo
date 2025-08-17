package io.github.ptitjes.konvo.core.agents

import io.github.ptitjes.konvo.core.ai.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.models.*

class AgentFactory(
    private val modelProviderManager: ModelManager,
    private val promptProviderManager: ProviderManager<PromptCard>,
    private val toolProviders: ProviderManager<ToolCard>,
    private val characterProviderManager: CharacterCardManager,
) {

    suspend fun createAgent(agentConfiguration: AgentConfiguration): Agent {
        return when (agentConfiguration) {
            is QuestionAnswerAgentConfiguration -> buildQuestionAnswerAgent(
                model = modelProviderManager.named(agentConfiguration.modelName),
                tools = agentConfiguration.toolNames.map { toolProviders.named(it) },
            )

            is RoleplayAgentConfiguration -> buildRoleplayAgent(
                model = modelProviderManager.named(agentConfiguration.modelName),
                character = characterProviderManager.named(agentConfiguration.characterName),
                characterGreetingIndex = agentConfiguration.characterGreetingIndex,
                userName = agentConfiguration.userName,
            )

            is NoAgentConfiguration -> error("No agent configured for this conversation")
        }
    }
}
