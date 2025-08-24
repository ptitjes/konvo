package io.github.ptitjes.konvo.core.conversations

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.storage.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

class ConversationManager(
    coroutineContext: CoroutineContext,
    private val conversationRepository: ConversationRepository,
    private val agentFactory: AgentFactory,
) {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception caught" }
    }

    private val coroutineScope = CoroutineScope(coroutineContext + job + handler)

    private val conversations = atomic(mapOf<String, Conversation>())

    fun getConversation(conversationId: String): Conversation {
        val updatedLiveConversations = conversations.updateAndGet {
            if (it.containsKey(conversationId)) it
            else it + (conversationId to buildLiveConversation(conversationId))
        }

        return updatedLiveConversations[conversationId] ?: error("Invalid state")
    }

    private fun buildLiveConversation(conversationId: String): Conversation {
        return Conversation(
            coroutineContext = coroutineScope.coroutineContext,
            conversationId = conversationId,
            repository = conversationRepository,
            agentFactory = agentFactory,
        )
    }
}
