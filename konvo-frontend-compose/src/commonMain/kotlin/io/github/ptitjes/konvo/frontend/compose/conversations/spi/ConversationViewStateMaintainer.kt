package io.github.ptitjes.konvo.frontend.compose.conversations.spi

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.ConversationViewState.*
import io.github.ptitjes.konvo.frontend.compose.conversations.spi.ConversationViewStates.*
import kotlin.reflect.*

suspend fun ConversationViewStateMaintainer.handleTranscript(transcript: List<Action<*>>) {
    for (element in transcript) handleEvent(element)
}

class ConversationViewStateMaintainer(
    initialState: Loaded = Loaded(),
) {
    var state = initialState
        private set

    private typealias StateUpdaterFunction<S, P> = suspend (state: S, event: Action<P>) -> S

    private val registeredStateUpdater =
        mutableMapOf<KClass<out Action.Payload>, MutableList<StateUpdaterFunction<Loaded, Action.Payload>>>()

    private fun <P : Action.Payload> addStateUpdater(
        klass: KClass<out P>,
        handler: StateUpdaterFunction<Loaded, P>,
    ): () -> Unit {
        registeredStateUpdater[klass] = (registeredStateUpdater[klass] ?: mutableListOf()).also {
            @Suppress("UNCHECKED_CAST")
            it += handler as StateUpdaterFunction<Loaded, Action.Payload>
        }
        return { registeredStateUpdater[klass]?.remove(handler) }
    }

    suspend fun handleEvent(event: Action<*>) {
        val payload = event.payload
        val updaters = registeredStateUpdater[payload::class]?.toList() ?: return
        state = updaters.fold(state) { state, updater -> updater(state, event) }
    }

    fun contributeViewStates(contribution: Contribution) {
        val scope = ContributionScopeImpl(this)
        with(contribution) { scope.contribute() }
    }

    private class ContributionScopeImpl(
        private val stateMaintainer: ConversationViewStateMaintainer,
    ) : ContributionScope() {
        override fun <S> Slot.Sequence<S>.invoke(
            contribute: ProducerScope<SequenceBuilder<S>>.() -> Unit,
        ) {
            val scope = SequenceProducerScopeImpl(stateMaintainer, this)
            scope.contribute()
        }

        override fun <K, S> Slot.Dictionary<K, S>.invoke(
            contribute: ProducerScope<DictionaryBuilder<K, S>>.() -> Unit,
        ) {
            val scope = DictionaryProducerScopeImpl(stateMaintainer, this)
            scope.contribute()
        }

        override fun <S> Slot.Register<S>.invoke(
            contribute: ProducerScope<RegisterBuilder<S>>.() -> Unit,
        ) {
            val scope = RegisterProducerScopeImpl(stateMaintainer, this)
            scope.contribute()
        }
    }

    private class SequenceProducerScopeImpl<S>(
        private val stateMaintainer: ConversationViewStateMaintainer,
        private val slot: Slot.Sequence<S>,
    ) : ProducerScope<SequenceBuilder<S>>() {

        override fun <P : Action.Payload> onEvent(
            klass: KClass<P>,
            action: suspend SequenceBuilder<S>.(Action<P>) -> Unit,
        ) {
            stateMaintainer.addStateUpdater(klass) { state, event ->
                val scope = SequenceBuilderImpl(
                    stateMaintainer = stateMaintainer,
                    slot = slot,
                    initialRootState = state,
                )
                scope.action(event)
                scope.rootState
            }
        }
    }

    private class SequenceBuilderImpl<S>(
        private val stateMaintainer: ConversationViewStateMaintainer,
        private val slot: Slot.Sequence<S>,
        initialRootState: Loaded,
    ) : SequenceBuilder<S> {
        var rootState: Loaded = initialRootState
            private set

        override suspend fun <T : S> append(
            value: suspend () -> T,
            builder: UpdaterScope<S, T>.() -> Unit,
        ) {
            val (updatedRootState, lens) = slot.append(rootState, value)
            rootState = updatedRootState

            UpdaterScopeImpl<S, T>(stateMaintainer, lens).builder()
        }
    }

    private class DictionaryProducerScopeImpl<K, S>(
        private val stateMaintainer: ConversationViewStateMaintainer,
        private val slot: Slot.Dictionary<K, S>,
    ) : ProducerScope<DictionaryBuilder<K, S>>() {

        override fun <P : Action.Payload> onEvent(
            klass: KClass<P>,
            action: suspend DictionaryBuilder<K, S>.(Action<P>) -> Unit,
        ) {
            stateMaintainer.addStateUpdater(klass) { state, event ->
                val scope = DictionaryBuilderImpl<K, S>(
                    stateMaintainer = stateMaintainer,
                    slot = slot,
                    initialRootState = state,
                )
                scope.action(event)
                scope.rootState
            }
        }
    }

    private class DictionaryBuilderImpl<K, S>(
        private val stateMaintainer: ConversationViewStateMaintainer,
        private val slot: Slot.Dictionary<K, S>,
        initialRootState: Loaded,
    ) : DictionaryBuilder<K, S> {
        var rootState: Loaded = initialRootState
            private set

        override suspend fun <T : S> put(
            key: K,
            value: suspend () -> T,
            builder: UpdaterScope<S?, T?>.() -> Unit,
        ) {
            val (updatedRootState, lens) = slot.put(rootState, key, value)
            rootState = updatedRootState

            UpdaterScopeImpl<S?, T?>(stateMaintainer, lens).builder()
        }
    }

    private class RegisterProducerScopeImpl<S>(
        private val stateMaintainer: ConversationViewStateMaintainer,
        private val slot: Slot.Register<S>,
    ) : ProducerScope<RegisterBuilder<S>>() {

        override fun <P : Action.Payload> onEvent(
            klass: KClass<P>,
            action: suspend RegisterBuilder<S>.(Action<P>) -> Unit,
        ) {
            stateMaintainer.addStateUpdater(klass) { state, event ->
                val scope = RegisterBuilderImpl<S>(
                    stateMaintainer = stateMaintainer,
                    slot = slot,
                    initialRootState = state,
                )
                scope.action(event)
                scope.rootState
            }
        }
    }

    private class RegisterBuilderImpl<S>(
        private val stateMaintainer: ConversationViewStateMaintainer,
        private val slot: Slot.Register<S>,
        initialRootState: Loaded,
    ) : RegisterBuilder<S> {
        var rootState: Loaded = initialRootState
            private set

        override suspend fun set(
            value: suspend (S) -> S,
            builder: UpdaterScope<S, S>.() -> Unit,
        ) {
            val (updatedRootState, lens) = slot.set(rootState, value)
            rootState = updatedRootState

            UpdaterScopeImpl(stateMaintainer, lens).builder()
        }
    }

    private class UpdaterScopeImpl<S, T : S>(
        private val stateMaintainer: ConversationViewStateMaintainer,
        private val lens: Slot.Lens<S>,
    ) : UpdaterScope<S, T>() {
        private val teardowns = mutableListOf<() -> Unit>()

        private val handlerScope = object : UpdateHandlerScope {
            override fun freeze() {
                this@UpdaterScopeImpl.teardowns.forEach { it() }
            }
        }

        override fun <Q : Action.Payload> onEvent(
            klass: KClass<Q>,
            handler: suspend UpdateHandlerScope.(T, Action<Q>) -> T,
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
