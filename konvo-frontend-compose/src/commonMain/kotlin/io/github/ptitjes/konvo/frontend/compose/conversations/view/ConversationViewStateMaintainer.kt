package io.github.ptitjes.konvo.frontend.compose.conversations.view

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.ConversationViewState.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.dsl.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.dsl.ConversationViewStateContribution.*
import kotlin.reflect.*

suspend fun ConversationViewStateMaintainer.handleTranscript(transcript: List<Event<*>>) {
    for (element in transcript) handleEvent(element)
}

class ConversationViewStateMaintainer(
    initialState: Loaded,
) : ConversationViewStateContribution {
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

    override fun contributeViewStates(builder: ContributionsScope.() -> Unit) {
        ContributionsScopeImpl(this).builder()
    }

    private class ContributionsScopeImpl(
        private val stateMaintainer: ConversationViewStateMaintainer,
    ) : ContributionsScope() {
        override fun <P : Event.Payload> onEvent(
            klass: KClass<P>,
            action: suspend ContributionScope.(Event<P>) -> Unit,
        ) {
            stateMaintainer.addStateUpdater(klass) { state, event ->
                val scope = ContributionScopeImpl(stateMaintainer, state)
                scope.action(event)
                scope.rootState
            }
        }
    }

    private class ContributionScopeImpl(
        private val stateMaintainer: ConversationViewStateMaintainer,
        initialRootState: Loaded,
    ) : ContributionScope {
        var rootState: Loaded = initialRootState
            private set

        override suspend fun <S, T : S> append(
            slot: Slot.Sequence<S>,
            initial: T,
            builder: UpdateHandlersScope<S, T>.() -> Unit,
        ) {
            val (updatedRootState, lens) = slot.append(rootState, initial)
            rootState = updatedRootState

            UpdateHandlersScopeImpl<S, T>(stateMaintainer, lens).builder()
        }

        override suspend fun <K, S, T : S> put(
            slot: Slot.Dictionary<K, S>,
            key: K,
            initial: T,
            builder: UpdateHandlersScope<S?, T?>.() -> Unit,
        ) {
            val (updatedRootState, lens) = slot.put(rootState, key, initial)
            rootState = updatedRootState

            UpdateHandlersScopeImpl<S?, T?>(stateMaintainer, lens).builder()
        }

        override suspend fun <S, T : S> set(
            slot: Slot.Register<S>,
            initial: T,
            builder: UpdateHandlersScope<S, T>.() -> Unit,
        ) {
            val (updatedRootState, lens) = slot.set(rootState, initial)
            rootState = updatedRootState

            UpdateHandlersScopeImpl<S, T>(stateMaintainer, lens).builder()
        }
    }

    private class UpdateHandlersScopeImpl<S, T : S>(
        private val stateMaintainer: ConversationViewStateMaintainer,
        private val lens: Slot.Lens<S>,
    ) : UpdateHandlersScope<S, T>() {
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
