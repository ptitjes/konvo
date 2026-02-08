package io.github.ptitjes.konvo.frontend.compose.conversations.spi

import io.github.ptitjes.konvo.core.conversations.model.*
import kotlin.reflect.*

sealed interface ConversationViewStates {

    fun interface Contribution {
        fun ContributionScope.contribute()
    }

    @DslMarker
    annotation class Marker

    @Marker
    abstract class ContributionScope {
        abstract operator fun <S> ConversationViewState.Slot.Sequence<S>.invoke(contribute: ProducerScope<SequenceBuilder<S>>.() -> Unit)
        abstract operator fun <K, S> ConversationViewState.Slot.Dictionary<K, S>.invoke(contribute: ProducerScope<DictionaryBuilder<K, S>>.() -> Unit)
        abstract operator fun <S> ConversationViewState.Slot.Register<S>.invoke(contribute: ProducerScope<RegisterBuilder<S>>.() -> Unit)
    }

    @Marker
    abstract class ProducerScope<Builder> {
        inline fun <reified P : Event.Payload> onEvent(
            noinline action: suspend Builder.(Event<P>) -> Unit,
        ) = onEvent(P::class, action)

        abstract fun <P : Event.Payload> onEvent(
            klass: KClass<P>,
            action: suspend Builder.(Event<P>) -> Unit,
        )
    }

    @Marker
    interface SequenceBuilder<S> {
        suspend fun <T : S> append(initial: T, builder: UpdaterScope<S, T>.() -> Unit = {})
    }

    @Marker
    interface DictionaryBuilder<K, S> {
        suspend fun <T : S> put(key: K, initial: T, builder: UpdaterScope<S?, T?>.() -> Unit = {})
    }

    @Marker
    interface RegisterBuilder<S> {
        suspend fun <T : S> set(initial: T, builder: UpdaterScope<S, T>.() -> Unit = {})
    }

    @Marker
    abstract class UpdaterScope<S, T : S> {
        inline fun <reified Q : Event.Payload> onEvent(
            noinline handler: suspend UpdateHandlerScope.(T, Event<Q>) -> T,
        ) = onEvent(Q::class, handler)

        abstract fun <Q : Event.Payload> onEvent(
            klass: KClass<Q>,
            handler: suspend UpdateHandlerScope.(T, Event<Q>) -> T,
        )
    }

    @Marker
    interface UpdateHandlerScope {
        fun freeze()
    }
}
