package io.github.ptitjes.konvo.core.agents

import io.github.ptitjes.konvo.core.characters.*
import io.github.ptitjes.konvo.core.models.*
import io.github.ptitjes.konvo.core.prompts.*
import io.github.ptitjes.konvo.core.tools.*
import kotlinx.coroutines.flow.*

class AgentFactory(
    private val modelProviderManager: ModelManager,
    private val promptProviderManager: PromptManager,
    private val toolProviders: ToolManager,
    private val characterProviderManager: CharacterManager,
) {

    suspend fun createAgent(agentConfiguration: AgentConfiguration): Agent {
        return when (agentConfiguration) {
            is QuestionAnswerAgentConfiguration -> buildQuestionAnswerAgent(
                model = modelProviderManager.named(agentConfiguration.modelName),
                tools = run {
                                    val available = toolProviders.tools.first()
                                    agentConfiguration.toolNames.map { name ->
                                        available.firstOrNull { it.name == name } ?: error("Model not found: $name")
                                    }
                                },
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
