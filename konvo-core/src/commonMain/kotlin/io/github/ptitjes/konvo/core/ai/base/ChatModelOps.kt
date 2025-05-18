package io.github.ptitjes.konvo.core.ai.base

import io.github.ptitjes.konvo.core.ai.spi.*
import kotlinx.coroutines.flow.*

suspend fun ChatModel.chat(
    userMessage: ChatMessage.User,
    vetoToolCalls: suspend (calls: List<VetoableToolCall>) -> Unit = {},
): Flow<ChatMessage> = chat(Unit, userMessage, vetoToolCalls)
