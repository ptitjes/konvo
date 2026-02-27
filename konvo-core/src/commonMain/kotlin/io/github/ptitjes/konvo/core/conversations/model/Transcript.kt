package io.github.ptitjes.konvo.core.conversations.model

import kotlinx.atomicfu.*

class Transcript {
    private val _events = atomic<List<Action<*>>>(emptyList())
    val events: List<Action<*>> get() = _events.value

    /**
     * Append a new event to the transcript.
     */
    fun append(event: Action<*>) {
        _events.update { it + event }
    }

    fun clear() {
        _events.update { emptyList() }
    }
}
