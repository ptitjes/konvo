package io.github.ptitjes.konvo.frontend.compose.conversations.spi

import io.github.ptitjes.konvo.core.conversations.model.Event
import kotlin.reflect.KClass

sealed interface ConversationViewStates {

    fun interface Contribution {
        fun CreateScope.contribute()
    }

    @DslMarker
    annotation class Marker

    @Marker
    abstract class CreateScope {
        inline fun <reified P : Event.Payload> onEvent(
            noinline action: suspend CreateHandlerScope.(Event<P>) -> Unit,
        ) = onEvent(P::class, action)

        abstract fun <P : Event.Payload> onEvent(
            klass: KClass<P>,
            action: suspend CreateHandlerScope.(Event<P>) -> Unit,
        )
    }

    @Marker
    interface CreateHandlerScope {

        suspend fun <S, T : S> append(
            slot: ConversationViewState.Slot.Sequence<S>,
            initial: T,
            builder: UpdateScope<S, T>.() -> Unit = {},
        )

        suspend fun <K, S, T : S> put(
            slot: ConversationViewState.Slot.Dictionary<K, S>,
            key: K,
            initial: T,
            builder: UpdateScope<S?, T?>.() -> Unit = {},
        )

        suspend fun <S, T : S> set(
            slot: ConversationViewState.Slot.Register<S>,
            initial: T,
            builder: UpdateScope<S, T>.() -> Unit = {},
        )
    }

    @Marker
    abstract class UpdateScope<S, T : S> {
        inline fun <reified Q : Event.Payload> onEvent(
            noinline handler: suspend UpdateHandlerScope.(T, Event<Q>) -> T,
        ) = onEvent(Q::class, handler)

        abstract fun <Q : Event.Payload> onEvent(
            klass: KClass<Q>,
            handler: suspend UpdateHandlerScope.(T, Event<Q>) -> T,
        )
    }

    interface UpdateHandlerScope {
        fun freeze()
    }
}
