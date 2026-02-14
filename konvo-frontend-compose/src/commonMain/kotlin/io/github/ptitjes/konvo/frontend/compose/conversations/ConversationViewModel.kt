package io.github.ptitjes.konvo.frontend.compose.conversations

import androidx.lifecycle.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.*
import io.github.ptitjes.konvo.frontend.compose.conversations.views.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.time.*

/**
 * ViewModel for the conversation UI.
 */
@OptIn(ExperimentalTime::class, FlowPreview::class)
class ConversationViewModel(
    conversationManager: ConversationManager,
    // TODO
    // private val viewStateContributions: Set<ConversationViewStates.Contribution>,
    // private val componentContributions: Set<ConversationComponents.Contribution>,
    private val conversationId: String,
) : ViewModel() {
    private val liveConversation = conversationManager.getConversation(conversationId)

    val conversation: ConversationUserView get() = liveConversation.newUserView()

    private val _state = MutableStateFlow<ConversationViewState>(ConversationViewState.Loading)
    val state: StateFlow<ConversationViewState> = _state

    // TODO
    // private fun ConversationViewStateMaintainer.setupViewStateContributions() {
    //     viewStateContributions.forEach { contributeViewStates(it) }
    // }

    init {
        println("Initializing ConversationViewModel(${this.conversationId})")
        viewModelScope.launch {
            launch {
                var previousTranscript: List<Event<*>>? = null

                liveConversation.awaitConversationLoaded()

                val conversationUserView = liveConversation.newUserView()

                conversationUserView.state.collect { state ->
                    when (state) {
                        is ConversationState.Loading -> {}
                        is ConversationState.Loaded -> {
                            val transcript = state.transcript
                            if (transcript != previousTranscript) {
                                val initial = ConversationViewState.Loaded()
                                    .copy(slot = ConversationViewState.Digest, value = state.digest)

                                val stateUpdater = ConversationViewStateMaintainer(initial)
                                stateUpdater.setupCoreViewStateProducers()
                                stateUpdater.handleTranscript(transcript)
                                val finalState = stateUpdater.state

                                previousTranscript = transcript
                                _state.value = finalState
                            }
                        }
                    }
                }
            }
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
