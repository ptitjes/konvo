package io.github.ptitjes.konvo.core.conversation.model

import kotlinx.atomicfu.*

class Transcript {
    private val _events = atomic<List<Event>>(emptyList())
    val events: List<Event> get() = _events.value

    /**
     * Append a new event to the transcript.
     */
    fun append(event: Event) {
        _events.update { it + event }
    }
}
