package io.github.ptitjes.konvo.frontend.compose.conversations.view

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.ConversationViewState.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.dsl.ConversationViewStates.*
import kotlin.reflect.*

suspend fun ConversationViewStateMaintainer.handleTranscript(transcript: List<Event<*>>) {
    for (element in transcript) handleEvent(element)
}

class ConversationViewStateMaintainer(
    initialState: Loaded,
) {
    var state = initialState
        private set

    private typealias StateUpdaterFunction<S, P> = suspend (state: S, event: Event<P>) -> S

    private val registeredStateUpdater =
        mutableMapOf<KClass<out Event.Payload>, MutableList<StateUpdaterFunction<Loaded, Event.Payload>>>()

    private fun <P : Event.Payload> addStateUpdater(
        klass: KClass<out P>,
        handler: StateUpdaterFunction<Loaded, P>,
    ): () -> Unit {
        registeredStateUpdater[klass] = (registeredStateUpdater[klass] ?: mutableListOf()).also {
            @Suppress("UNCHECKED_CAST")
            it += handler as StateUpdaterFunction<Loaded, Event.Payload>
        }
        return { registeredStateUpdater[klass]?.remove(handler) }
    }

    suspend fun handleEvent(event: Event<*>) {
        val payload = event.payload
        val updaters = registeredStateUpdater[payload::class]?.toList() ?: return
        state = updaters.fold(state) { state, updater -> updater(state, event) }
    }

    fun contributeViewStates(contribution: Contribution) {
        val scope = CreateScopeImpl(this)
        with(contribution) { scope.contribute() }
    }

    private class CreateScopeImpl(
        private val stateMaintainer: ConversationViewStateMaintainer,
    ) : CreateScope() {
        override fun <P : Event.Payload> onEvent(
            klass: KClass<P>,
            action: suspend CreateHandlerScope.(Event<P>) -> Unit,
        ) {
            stateMaintainer.addStateUpdater(klass) { state, event ->
                val scope = CreateHandlerScopeImpl(stateMaintainer, state)
                scope.action(event)
                scope.rootState
            }
        }
    }

    private class CreateHandlerScopeImpl(
        private val stateMaintainer: ConversationViewStateMaintainer,
        initialRootState: Loaded,
    ) : CreateHandlerScope {
        var rootState: Loaded = initialRootState
            private set

        override suspend fun <S, T : S> append(
            slot: Slot.Sequence<S>,
            initial: T,
            builder: UpdateScope<S, T>.() -> Unit,
        ) {
            val (updatedRootState, lens) = slot.append(rootState, initial)
            rootState = updatedRootState

            UpdateScopeImpl<S, T>(stateMaintainer, lens).builder()
        }

        override suspend fun <K, S, T : S> put(
            slot: Slot.Dictionary<K, S>,
            key: K,
            initial: T,
            builder: UpdateScope<S?, T?>.() -> Unit,
        ) {
            val (updatedRootState, lens) = slot.put(rootState, key, initial)
            rootState = updatedRootState

            UpdateScopeImpl<S?, T?>(stateMaintainer, lens).builder()
        }

        override suspend fun <S, T : S> set(
            slot: Slot.Register<S>,
            initial: T,
            builder: UpdateScope<S, T>.() -> Unit,
        ) {
            val (updatedRootState, lens) = slot.set(rootState, initial)
            rootState = updatedRootState

            UpdateScopeImpl<S, T>(stateMaintainer, lens).builder()
        }
    }

    private class UpdateScopeImpl<S, T : S>(
        private val stateMaintainer: ConversationViewStateMaintainer,
        private val lens: Slot.Lens<S>,
    ) : UpdateScope<S, T>() {
        private val teardowns = mutableListOf<() -> Unit>()

        private val handlerScope = object : UpdateHandlerScope {
            override fun freeze() {
                teardowns.forEach { it() }
            }
        }

        override fun <Q : Event.Payload> onEvent(
            klass: KClass<Q>,
            handler: suspend UpdateHandlerScope.(T, Event<Q>) -> T,
        ) {
            teardowns += stateMaintainer.addStateUpdater(klass) { state, event ->
                lens.update(state) { previousState ->
                    @Suppress("UNCHECKED_CAST")
                    handlerScope.handler(previousState as T, event)
                }
            }
        }
    }
}
