package io.github.ptitjes.konvo.frontend.compose.conversations.view

import io.github.ptitjes.konvo.core.conversations.model.Event
import kotlin.reflect.KClass

suspend fun ConversationStateMaintainer.handleTranscript(transcript: List<Event>) {
    for (element in transcript) handleEvent(element)
}

class ConversationStateMaintainer(
    initialState: ConversationViewState.Loaded,
) {
    var state = initialState
        private set

    private val registeredStateUpdater =
        mutableMapOf<KClass<out Event.Payload>, MutableList<suspend (ConversationViewState.Loaded, Event, Event.Payload) -> ConversationViewState.Loaded>>()

    inline fun <reified P : Event.Payload> addStateUpdater(
        noinline handler: suspend (ConversationViewState.Loaded, Event, P) -> ConversationViewState.Loaded,
    ): () -> Unit = addStateUpdater(P::class, handler)

    fun <P : Event.Payload> addStateUpdater(
        klass: KClass<out P>,
        handler: suspend (ConversationViewState.Loaded, Event, P) -> ConversationViewState.Loaded,
    ): () -> Unit {
        registeredStateUpdater[klass] = (registeredStateUpdater[klass] ?: mutableListOf()).also {
            @Suppress("UNCHECKED_CAST")
            it += handler as suspend (ConversationViewState.Loaded, Event, Event.Payload) -> ConversationViewState.Loaded
        }
        return { registeredStateUpdater[klass]?.remove(handler) }
    }

    suspend fun handleEvent(event: Event) {
        val payload = event.payload
        val updaters = this@ConversationStateMaintainer.registeredStateUpdater[payload::class]?.toList() ?: return
        state = updaters.fold(state) { state, updater -> updater(state, event, payload) }
    }

    @DslMarker
    annotation class EventContributionDslMarker

    @EventContributionDslMarker
    inline fun <reified P : Event.Payload> onEvent(
        crossinline action: suspend ContributionBuilderScope.(Event, P) -> Unit,
    ) {
        addStateUpdater<P> { state, event, payload ->
            val scope = ContributionBuilderScope(this, state)
            scope.action(event, payload)
            scope.state
        }
    }

    @EventContributionDslMarker
    class ContributionBuilderScope(
        private val stateUpdater: ConversationStateMaintainer,
        initialState: ConversationViewState.Loaded,
    ) {
        var state: ConversationViewState.Loaded = initialState
            private set

        fun <S : ItemViewState> contributeItem(
            initialViewState: S,
            builder: HandlersBuilderScope<S>.() -> Unit = {},
        ) {
            state = state.copy(items = state.items + initialViewState)

            HandlersBuilderScope(stateUpdater, initialViewState).builder()
        }
    }

    @EventContributionDslMarker
    class HandlersBuilderScope<S : ItemViewState>(
        private val stateUpdater: ConversationStateMaintainer,
        private val initialViewState: S,
    ) {
        private val teardowns = mutableListOf<() -> Unit>()

        private val handlerScope = object : HandlerBuilderScope() {
            override fun freezeItem() {
                this@HandlersBuilderScope.teardowns.forEach { it() }
            }
        }

        inline fun <reified Q : Event.Payload> onEvent(
            noinline handler: suspend HandlerBuilderScope.(S, Q) -> S,
        ) = onEvent(Q::class, handler)

        fun <Q : Event.Payload> onEvent(
            klass: KClass<Q>,
            handler: suspend HandlerBuilderScope.(S, Q) -> S,
        ) {
            teardowns += stateUpdater.addStateUpdater(klass) { state, event, payload ->
                val itemViewStateIndex = state.items.indexOfLast { it.id == initialViewState.id }
                require(itemViewStateIndex != -1) { "No view state found for initial view state" }
                val itemViewState = state.items[itemViewStateIndex]

                @Suppress("UNCHECKED_CAST")
                val updatedItemViewState = handlerScope.handler(itemViewState as S, payload)
                state.copy(
                    items = state.items.mapIndexed { index, itemViewState ->
                        if (index == itemViewStateIndex) updatedItemViewState else itemViewState
                    }
                )
            }
        }
    }

    @EventContributionDslMarker
    abstract class HandlerBuilderScope {
        abstract fun freezeItem()
    }
}