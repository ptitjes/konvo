package io.github.ptitjes.konvo.frontend.compose.conversations.view

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.states.*

sealed interface ConversationViewState {
    data object Loading : ConversationViewState

    @ConsistentCopyVisibility
    data class Loaded private constructor(
        private val slotData: Map<Slot<*>, Any?>,
    ) : ConversationViewState {

        constructor() : this(emptyMap())

        @Suppress("UNCHECKED_CAST")
        operator fun <T : Indexed> get(slot: AppendableSlot<T>): List<T> =
            (slotData[slot] ?: emptyList<T>()) as List<T>

        @Suppress("UNCHECKED_CAST")
        operator fun <T> get(slot: RegisterSlot<T>): T = (slotData[slot] ?: slot.defaultValue) as T

        fun <T : Indexed> copy(slot: AppendableSlot<T>, value: List<T>): Loaded =
            copy(slotData = slotData + (slot to value))

        fun <T> copy(slot: RegisterSlot<T>, value: T): Loaded =
            copy(slotData = slotData + (slot to value))

        // TODO Remove the following!
        val conversation: ConversationDigest get() = get(Digest)!!
        val items: List<Item> get() = get(Items)
        val agentPresence: AgentPresenceViewState.Presence? get() = get(AgentState)

        val isProcessing: Boolean
            get() = agentPresence?.status is AgentPresenceViewState.Presence.Status.Processing
    }

    interface Indexed {
        val id: Any
    }

    interface Slot<T> {
        suspend fun update(rootState: Loaded, initialEvent: Event<*>, updater: suspend (T) -> T): Loaded
    }

    open class AppendableSlot<T : Indexed> : Slot<T> {
        suspend fun append(rootState: Loaded, childState: T): Loaded =
            rootState.copy(slot = this, value = rootState[this] + childState)

        override suspend fun update(
            rootState: Loaded,
            initialEvent: Event<*>,
            updater: suspend (T) -> T,
        ): Loaded {
            val items = rootState[this]
            val itemIndex = items.indexOfLast { it.id == initialEvent.id }
            require(itemIndex != -1) { "No view state found for initial view state" }
            val previousItem = items[itemIndex]
            val updatedItem = updater(previousItem)
            return rootState.copy(slot = this, value = items.mapIndexed { index, item ->
                if (index == itemIndex) updatedItem else item
            })
        }
    }

    open class RegisterSlot<T>(
        val defaultValue: T,
    ) : Slot<T> {
        suspend fun set(rootState: Loaded, childState: T): Loaded =
            rootState.copy(slot = this, value = childState)

        override suspend fun update(
            rootState: Loaded,
            initialEvent: Event<*>,
            updater: suspend (T) -> T,
        ): Loaded {
            val value = rootState[this]
            val updatedValue = updater(value)
            return rootState.copy(slot = this, value = updatedValue)
        }
    }

    object Digest : RegisterSlot<ConversationDigest?>(null)

    object AgentState : RegisterSlot<AgentPresenceViewState.Presence?>(null)

    interface Item : Indexed

    object Items : AppendableSlot<Item>()
}
