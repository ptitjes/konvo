package io.github.ptitjes.konvo.frontend.compose.conversations.view.dsl

import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.frontend.compose.conversations.view.*
import kotlin.reflect.*

interface ConversationViewStateContribution {

    @DslMarker
    annotation class Marker

    @Marker
    fun contributeViewStates(contribution: Contribution) = contributeViewStates {
        with(contribution) { this@contributeViewStates.contribute() }
    }

    fun interface Contribution {
        fun ContributionsScope.contribute()
    }

    @Marker
    fun contributeViewStates(builder: ContributionsScope.() -> Unit)

    @Marker
    abstract class ContributionsScope {
        inline fun <reified P : Event.Payload> onEvent(
            noinline action: suspend ContributionScope.(Event<P>) -> Unit,
        ) = onEvent(P::class, action)

        abstract fun <P : Event.Payload> onEvent(
            klass: KClass<P>,
            action: suspend ContributionScope.(Event<P>) -> Unit,
        )
    }

    @Marker
    interface ContributionScope {
        suspend fun <S, T : S> set(
            slot: ConversationViewState.RegisterSlot<S>,
            initial: T,
            builder: UpdateHandlersScope<S, T>.() -> Unit = {},
        )

        suspend fun <S, T : S> append(
            slot: ConversationViewState.AppendableSlot<S>,
            initial: T,
            builder: UpdateHandlersScope<S, T>.() -> Unit = {},
        )
    }

    @Marker
    abstract class UpdateHandlersScope<S, T : S> {
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
