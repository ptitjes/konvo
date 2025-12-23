package io.github.ptitjes.konvo.frontend.compose.conversations.view

import io.github.ptitjes.konvo.core.conversations.model.*

sealed interface ConversationViewState {
    data object Loading : ConversationViewState
    data class Loaded(
        val conversation: ConversationDigest,
        val items: List<Item>,
        val isProcessing: Boolean,
    ) : ConversationViewState

    interface Slot<T> {
        suspend fun update(rootState: Loaded, initialEvent: Event<*>, updater: suspend (T) -> T): Loaded
    }

    interface AppendableSlot<T> : Slot<T> {
        suspend fun get(rootState: Loaded): List<T>
        suspend fun append(rootState: Loaded, childState: T): Loaded
    }

    interface RegisterSlot<T> : Slot<T> {
        suspend fun get(rootState: Loaded): T?
        suspend fun set(rootState: Loaded, childState: T): Loaded
    }

    object AgentState : RegisterSlot<Boolean> {
        override suspend fun get(rootState: Loaded): Boolean = rootState.isProcessing

        override suspend fun set(rootState: Loaded, childState: Boolean): Loaded =
            rootState.copy(isProcessing = childState)

        override suspend fun update(
            rootState: Loaded,
            initialEvent: Event<*>,
            updater: suspend (Boolean) -> Boolean,
        ): Loaded = rootState.copy(isProcessing = updater(rootState.isProcessing))
    }

    interface Item {
        val id: Any
    }

    object Items : AppendableSlot<Item> {
        override suspend fun get(rootState: Loaded): List<Item> = rootState.items

        override suspend fun append(rootState: Loaded, childState: Item): Loaded =
            rootState.copy(items = rootState.items + childState)

        override suspend fun update(
            rootState: Loaded,
            initialEvent: Event<*>,
            updater: suspend (Item) -> Item,
        ): Loaded {
            val itemIndex = rootState.items.indexOfLast { it.id == initialEvent.id }
            require(itemIndex != -1) { "No view state found for initial view state" }
            val previousItem = rootState.items[itemIndex]
            val updatedItem = updater(previousItem)
            return rootState.copy(items = rootState.items.mapIndexed { index, item ->
                if (index == itemIndex) updatedItem else item
            })
        }
    }
}
