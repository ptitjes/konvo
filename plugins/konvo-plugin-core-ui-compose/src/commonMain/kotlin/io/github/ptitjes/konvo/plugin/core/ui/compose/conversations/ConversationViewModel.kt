package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import androidx.lifecycle.*
import io.github.ptitjes.konvo.plugin.core.conversations.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.views.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

/**
 * ViewModel for the conversation UI.
 */
@OptIn(ExperimentalTime::class, FlowPreview::class)
class ConversationViewModel(
    private val conversationId: String,
    private val conversationRepository: ConversationRepository,
    conversationManager: ConversationManager,
    // TODO
    // private val viewStateContributions: Set<ConversationViewStates.Contribution>,
    // private val componentContributions: Set<ConversationComponents.Contribution>,
) : ViewModel() {
    private val liveConversation = conversationManager.getConversation(conversationId)

    val conversation: InteractionDevice.User get() = liveConversation.newUserDevice()

    private val _state = MutableStateFlow<ConversationViewState>(ConversationViewState.Loading)
    val state: StateFlow<ConversationViewState> = _state

    // TODO
    // private fun ConversationViewStateMaintainer.setupViewStateContributions() {
    //     viewStateContributions.forEach { contributeViewStates(it) }
    // }

    private val stateUpdater = ConversationViewStateMaintainer().apply {
        setupCoreViewStateProducers()
    }

    init {
        println("Initializing ConversationViewModel(${this.conversationId})")
        viewModelScope.launch {
            val state = liveConversation.awaitConversationLoaded()
            val conversationUserView = liveConversation.newUserDevice()

            val transcriptHandled = Job()

            launch {
                conversationUserView.actions.buffer(Channel.UNLIMITED).collect { event ->
                    transcriptHandled.join()

                    stateUpdater.handleEvent(event)
                    updateStateAndStoredDigest()
                }
            }

            // Extract actions from transcript for view state processing
            val actions = state.transcript.actions
            stateUpdater.handleTranscript(actions)
            updateStateAndStoredDigest()

            transcriptHandled.complete()
        }
    }

    private fun CoroutineScope.updateStateAndStoredDigest() {
        _state.value = stateUpdater.state

        launch {
            val digest = conversationRepository.getDigest(conversationId).first()
            conversationRepository.updateDigest(
                digest.updateFrom(stateUpdater.state, "user")
            )
        }
    }

    val componentRegistry: ConversationViewRegistry by lazy {
        ConversationViewRegistry.Builder().apply {
            contributeComponents(CoreComponents)
            // TODO
            // componentContributions.forEach { contributeComponents(it) }
        }.build()
    }

    override fun onCleared() {
        super.onCleared()
        println("Cleared ConversationViewModel(${this.conversationId})")
    }
}

private fun ConversationDigest.updateFrom(
    state: ConversationViewState.Loaded,
    participantId: String,
): ConversationDigest {
    val preview = state.preview
    val presence = state.presence
    val items = state.items

    val participant = presence.keys.firstOrNull { it.id == participantId } ?: return this
    val lastViewTimestamp = presence[participant]?.lastViewTimestamp

    return copy(
        title = preview.title,
        updatedAt = Clock.System.now(),
        participants = presence.keys.toList(),
        lastMessagePreview = preview.lastMessagePreview,
        messageCount = items.size,
        unreadMessageCount =
            if (lastViewTimestamp != null) items.count { it.timestamp > lastViewTimestamp } else items.size,
    )
}
