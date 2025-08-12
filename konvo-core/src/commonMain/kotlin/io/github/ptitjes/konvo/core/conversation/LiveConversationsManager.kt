package io.github.ptitjes.konvo.core.conversation

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.conversation.storage.*
import kotlinx.atomicfu.*
import kotlinx.coroutines.*
import kotlin.coroutines.*

class LiveConversationsManager(
    coroutineContext: CoroutineContext,
    private val conversationRepository: ConversationRepository,
) {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception caught" }
    }

    private val coroutineScope = CoroutineScope(coroutineContext + job + handler)

    private val liveConversations = atomic(mapOf<String, LiveConversation>())

    fun getLiveConversation(conversationId: String): LiveConversation {
        val updatedLiveConversations = liveConversations.updateAndGet { lc ->
            if (lc.containsKey(conversationId)) lc
            else {
                lc + (conversationId to buildLiveConversation(conversationId))
            }
        }

        return updatedLiveConversations[conversationId] ?: error("Invalid state")
    }

    private fun buildLiveConversation(conversationId: String): LiveConversation {
        return LiveConversation(
            coroutineContext = coroutineScope.coroutineContext,
            conversationId = conversationId,
            repository = conversationRepository,
        )
    }
}
