package io.github.ptitjes.konvo.plugin.core.agents

import kotlinx.serialization.*

@Serializable
data class QuestionAnswerAgentConfiguration(
    val mcpServerNames: Set<String>,
    val modelName: String,
) : AgentConfiguration
