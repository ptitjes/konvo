package io.github.ptitjes.konvo.frontend.compose.conversations.view

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.ConversationViewState.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.states.*

sealed interface ConversationViewState {
    data object Loading : ConversationViewState

    @ConsistentCopyVisibility
    data class Loaded private constructor(
        private val slotData: Map<Slot<*>, Any?>,
    ) : ConversationViewState {

        constructor() : this(emptyMap())

        @Suppress("UNCHECKED_CAST")
        operator fun <C> get(slot: Slot<C>): C = slotData[slot] as C? ?: slot.initialValue

        fun <C> copy(slot: Slot<C>, value: C): Loaded = copy(slotData = slotData + (slot to value))
    }

    interface Slot<C> {
        val initialValue: C

        interface Lens<T> {
            suspend fun update(rootState: Loaded, updater: suspend (T) -> T): Loaded
        }

        open class Sequence<T> : Slot<List<T>> {
            override val initialValue: List<T> get() = emptyList()

            suspend fun append(rootState: Loaded, childState: T): Pair<Loaded, Lens<T>> {
                val container = rootState[this]
                val newIndex = container.size
                val updatedContainer = container + childState
                val updatedRootState = rootState.copy(slot = this, value = updatedContainer)
                return updatedRootState to AppendableLens(newIndex)
            }

            private inner class AppendableLens(private val index: Int) : Lens<T> {
                override suspend fun update(rootState: Loaded, updater: suspend (T) -> T): Loaded {
                    val container = rootState[this@Sequence]
                    val value = container[index]
                    val updatedValue = updater(value)
                    val updatedContainer = container.toMutableList().apply { this[index] = updatedValue }
                    return rootState.copy(slot = this@Sequence, value = updatedContainer)
                }
            }
        }

        open class Dictionary<K, T> : Slot<Map<K, T>> {
            override val initialValue: Map<K, T> get() = emptyMap()

            suspend fun put(rootState: Loaded, key: K, childState: T): Pair<Loaded, Lens<T?>> {
                val container = rootState[this]
                val updatedContainer = container + (key to childState)
                return rootState.copy(slot = this@Dictionary, value = updatedContainer) to IndexLens(key)
            }

            private inner class IndexLens(private val key: K) : Lens<T?> {
                override suspend fun update(rootState: Loaded, updater: suspend (T?) -> T?): Loaded {
                    val container = rootState[this@Dictionary]
                    val value = container[key]
                    val updatedValue = updater(value)
                    val updatedContainer =
                        if (updatedValue == null) container - key else container + (key to updatedValue)
                    return rootState.copy(slot = this@Dictionary, value = updatedContainer)
                }
            }
        }

        open class Register<T>(val defaultValue: T) : Slot<T> {
            override val initialValue: T get() = defaultValue

            suspend fun set(rootState: Loaded, childState: T): Pair<Loaded, Lens<T>> {
                return rootState.copy(slot = this@Register, value = childState) to RegisterLens()
            }

            private inner class RegisterLens : Lens<T> {
                override suspend fun update(rootState: Loaded, updater: suspend (T) -> T): Loaded {
                    return rootState.copy(slot = this@Register, value = updater(rootState[this@Register]))
                }
            }
        }
    }

    object Digest : Slot.Register<ConversationDigest?>(null)

    object Agents : Slot.Dictionary<Participant.Agent, AgentViewState>()

    interface Item {
        val id: Any
    }

    object Items : Slot.Sequence<Item>()
}

// TODO Remove the following!
val Loaded.digest: ConversationDigest get() = get(Digest)!!
val Loaded.items: List<Item> get() = get(Items)
val Loaded.agents: Map<Participant.Agent, AgentViewState> get() = get(Agents)
val Loaded.isProcessing: Boolean get() = agents.any { (_, state) -> state.isProcessing }
