package io.github.ptitjes.konvo.core.agents

sealed interface AgentConfiguration

/** Default no-op configuration used when an agent is not specified or cannot be resolved. */
object NoAgentConfiguration : AgentConfiguration

data class QuestionAnswerAgentConfiguration(
    val mcpServerNames: Set<String>,
    val modelName: String,
) : AgentConfiguration

data class RoleplayAgentConfiguration(
    val characterId: String,
    val characterGreetingIndex: Int?,
    val personaName: String,
    val modelName: String,
    val lorebookId: String? = null,
    val scanDepthOverride: Int? = null,
    val tokenBudgetOverride: Int? = null,
    val recursiveScanningOverride: Boolean? = null,
) : AgentConfiguration
