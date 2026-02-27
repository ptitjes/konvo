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
        inline fun <reified P : Action.Payload> onEvent(
            noinline action: suspend Builder.(Action<P>) -> Unit,
        ) = onEvent(P::class, action)

        abstract fun <P : Action.Payload> onEvent(
            klass: KClass<P>,
            action: suspend Builder.(Action<P>) -> Unit,
        )
    }

    @Marker
    interface SequenceBuilder<S> {
        suspend fun <T : S> append(value: suspend () -> T, builder: UpdaterScope<S, T>.() -> Unit)
        suspend fun <T : S> append(value: suspend () -> T) = append(value) {}
    }

    @Marker
    interface DictionaryBuilder<K, S> {
        suspend fun <T : S> put(key: K, value: suspend () -> T, builder: UpdaterScope<S?, T?>.() -> Unit)
        suspend fun <T : S> put(key: K, value: suspend () -> T) = put(key, value) {}
    }

    @Marker
    interface RegisterBuilder<S> {
        suspend fun set(value: suspend (S) -> S, builder: UpdaterScope<S, S>.() -> Unit)
        suspend fun set(value: suspend (S) -> S) = set(value = value) {}
    }

    @Marker
    abstract class UpdaterScope<S, T : S> {
        inline fun <reified Q : Action.Payload> onEvent(
            noinline handler: suspend UpdateHandlerScope.(T, Action<Q>) -> T,
        ) = onEvent(Q::class, handler)

        abstract fun <Q : Action.Payload> onEvent(
            klass: KClass<Q>,
            handler: suspend UpdateHandlerScope.(T, Action<Q>) -> T,
        )
    }

    @Marker
    interface UpdateHandlerScope {
        fun freeze()
    }
}
