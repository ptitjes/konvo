package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi

import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi.ConversationViewState.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.views.*
import kotlin.time.*

@ConsistentCopyVisibility
data class ConversationViewState private constructor(
    private val slotStates: Map<Slot<*>, Any?>,
) {
    constructor() : this(emptyMap())

    @Suppress("UNCHECKED_CAST")
    operator fun <C> get(slot: Slot<C>): C =
        slotStates[slot] as C? ?: slot.initialValue

    fun <C> copy(slot: Slot<C>, value: C): ConversationViewState =
        copy(slotStates = slotStates + (slot to value))

    interface Slot<C> {
        val initialValue: C

        interface Lens<T> {
            suspend fun update(rootState: ConversationViewState, updater: suspend (T) -> T): ConversationViewState
        }

        open class Sequence<T> : Slot<List<T>> {
            override val initialValue: List<T> get() = emptyList()

            suspend fun append(
                rootState: ConversationViewState,
                value: suspend () -> T,
            ): Pair<ConversationViewState, Lens<T>> {
                val container = rootState[this]
                val newIndex = container.size
                val updatedContainer = container + value()
                val updatedRootState = rootState.copy(slot = this, value = updatedContainer)
                return updatedRootState to AppendableLens(newIndex)
            }

            private inner class AppendableLens(private val index: Int) : Lens<T> {
                override suspend fun update(
                    rootState: ConversationViewState,
                    updater: suspend (T) -> T,
                ): ConversationViewState {
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

            suspend fun put(
                rootState: ConversationViewState,
                key: K,
                value: suspend () -> T,
            ): Pair<ConversationViewState, Lens<T?>> {
                val container = rootState[this]
                val updatedContainer = container + (key to value())
                return rootState.copy(slot = this@Dictionary, value = updatedContainer) to IndexLens(key)
            }

            private inner class IndexLens(private val key: K) : Lens<T?> {
                override suspend fun update(
                    rootState: ConversationViewState,
                    updater: suspend (T?) -> T?,
                ): ConversationViewState {
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

            suspend fun set(
                rootState: ConversationViewState,
                updater: suspend (T) -> T,
            ): Pair<ConversationViewState, Lens<T>> {
                val lens = RegisterLens()
                return lens.update(rootState, updater) to lens
            }

            private inner class RegisterLens : Lens<T> {
                override suspend fun update(
                    rootState: ConversationViewState,
                    updater: suspend (T) -> T,
                ): ConversationViewState {
                    return rootState.copy(slot = this@Register, value = updater(rootState[this@Register]))
                }
            }
        }
    }

    object Preview : Slot.Register<PreviewViewState>(PreviewViewState())
    object Presence : Slot.Dictionary<Participant, PresenceViewState>()
    object Agents : Slot.Dictionary<Participant.Agent, AgentViewState>()

    interface Item {
        val id: Any
        val timestamp: Instant
    }

    object Items : Slot.Sequence<Item>()
}

// TODO Move the following alongside the slot definitions
val ConversationViewState.preview: PreviewViewState get() = get(Preview)
val ConversationViewState.presence: Map<Participant, PresenceViewState> get() = get(Presence)
val ConversationViewState.agents: Map<Participant.Agent, AgentViewState> get() = get(Agents)
val ConversationViewState.isProcessing: Boolean get() = agents.any { (_, state) -> state.isProcessing }
val ConversationViewState.items: List<Item> get() = get(Items)
