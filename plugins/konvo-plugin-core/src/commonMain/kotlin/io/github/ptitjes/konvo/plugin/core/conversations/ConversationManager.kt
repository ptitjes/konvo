package io.github.ptitjes.konvo.plugin.core.conversations

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.*
import kotlinx.coroutines.*
import kotlin.concurrent.atomics.*
import kotlin.coroutines.*
import kotlin.time.*
import kotlin.uuid.*

@OptIn(ExperimentalAtomicApi::class)
class ConversationManager(
    coroutineContext: CoroutineContext,
    private val conversationRepository: ConversationRepository,
    private val agentFactory: AgentManager,
) {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val job = SupervisorJob()
    private val handler = CoroutineExceptionHandler { _, exception ->
        logger.error(exception) { "Exception caught" }
    }

    private val coroutineScope = CoroutineScope(coroutineContext + job + handler)

    private val conversations = AtomicReference(mapOf<String, Conversation>())

    suspend fun newConversation(
        agentConfiguration: AgentConfiguration,
    ): Conversation = newConversation().also { conversation ->
        coroutineScope.launch {
            conversation.join()
            conversation.inviteAgent(agentConfiguration)
        }
    }

    private suspend fun newConversation(): Conversation {
        val id = Uuid.random().toString()
        val now = Clock.System.now()

        val conversation = ConversationDigest(
            id = id,
            createdAt = now,
            updatedAt = now,
        )

        conversationRepository.create(conversation)

        return getConversation(id)
    }

    fun getConversation(conversationId: String): Conversation {
        val updatedLiveConversations = conversations.updateAndFetch {
            if (it.containsKey(conversationId)) it
            else it + (conversationId to buildLiveConversation(conversationId))
        }

        return updatedLiveConversations[conversationId] ?: error("Invalid state")
    }

    private fun buildLiveConversation(conversationId: String): Conversation {
        return Conversation(
            coroutineContext = coroutineScope.coroutineContext,
            id = conversationId,
            repository = conversationRepository,
            agentFactory = agentFactory,
        )
    }
}
