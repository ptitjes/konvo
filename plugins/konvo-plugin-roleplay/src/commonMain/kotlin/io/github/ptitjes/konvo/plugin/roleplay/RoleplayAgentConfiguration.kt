package io.github.ptitjes.konvo.plugin.roleplay

import io.github.ptitjes.konvo.plugin.core.agents.*
import kotlinx.serialization.*

@Serializable
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
