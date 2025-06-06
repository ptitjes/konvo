package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.agents.core.dsl.builder.*
import ai.koog.prompt.message.*

fun AIAgentSubgraphBuilderBase<*, *>.nodeVetToolCall(
    name: String? = null,
    vetToolCall: suspend (Message.Tool.Call) -> Boolean,
) = node<Message.Tool.Call, VettedToolCall>(name) { VettedToolCall(it, vetToolCall(it)) }

data class VettedToolCall(
    val call: Message.Tool.Call,
    val vetted: Boolean,
)
