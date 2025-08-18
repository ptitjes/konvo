package io.github.ptitjes.konvo.core.agents

sealed interface AgentConfiguration

/** Default no-op configuration used when an agent is not specified or cannot be resolved. */
object NoAgentConfiguration : AgentConfiguration

data class QuestionAnswerAgentConfiguration(
    val toolNames: List<String>,
    val modelName: String,
) : AgentConfiguration

data class RoleplayAgentConfiguration(
    val characterId: String,
    val characterGreetingIndex: Int?,
    val userName: String,
    val modelName: String,
) : AgentConfiguration
