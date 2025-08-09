package io.github.ptitjes.konvo.core.conversation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

/**
 * A transcript of a conversation, keeping an ordered log of all conversation events.
 *
 * - Exposes a StateFlow of the current list of events for observers (e.g., UIs) to render snapshots.
 * - Provides an append API used by the conversation to record new events as they happen.
 */
class ConversationTranscript {
    private val _events = MutableStateFlow<List<ConversationEvent>>(emptyList())

    /**
     * Immutable view of the conversation events, in chronological order of insertion.
     */
    val events: StateFlow<List<ConversationEvent>> get() = _events

    /**
     * Append a new event to the transcript.
     */
    fun append(event: ConversationEvent) {
        _events.update { it + event }
    }
}
