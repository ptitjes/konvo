package io.github.ptitjes.konvo.plugin.core.agents.toolkit

import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import kotlinx.coroutines.*
import kotlin.reflect.*

@InteractiveAgentDsl
class InteractionBuilder<P : Action.Payload, S> internal constructor(
    internal val protocol: InteractionProtocol,
    internal val initialState: suspend context(AgentContext) (Action<P>) -> S,
) {
    private var enterHandler: InteractionDriver.Handler<P, S, Action<P>>? = null
    private var errorHandler: InteractionDriver.Handler<P, S, Throwable>? = null
    private var leaveHandler: InteractionDriver.Handler<P, S, Action<P>>? = null
    private var executeHandler: InteractionDriver.Handler<P, S, Action<P>>? = null
    private val eventHandlers = mutableMapOf<KClass<out Action.Payload>, InteractionDriver.Handler<P, S, Action<*>>>()

    internal fun build(): InteractionDriver<P, S> {
        return InteractionDriver(
            protocol = protocol,
            initialState = initialState,
            onEnter = enterHandler ?: {},
            onError = errorHandler ?: {},
            onLeave = leaveHandler ?: {},
            onExecute = executeHandler ?: { awaitCancellation() },
            onEvent = eventHandlers.toMap(),
        )
    }

    fun onEnter(handler: suspend context(AgentContext) InteractionScope<S>.(input: Action<P>) -> Unit) {
        enterHandler = handler
    }

    fun onError(handler: suspend context(AgentContext) InteractionScope<S>.(throwable: Throwable) -> Unit) {
        errorHandler = handler
    }

    fun onLeave(handler: suspend context(AgentContext) InteractionScope<S>.(input: Action<P>) -> Unit) {
        leaveHandler = handler
    }

    fun <E : Action.Payload> onAction(
        klass: KClass<E>,
        handler: suspend context(AgentContext) InteractionScope<S>.(event: Action<E>) -> Unit,
    ) {
        @Suppress("UNCHECKED_CAST")
        eventHandlers[klass] = handler as suspend context(AgentContext) InteractionScope<S>.(event: Action<*>) -> Unit
    }

    inline fun <reified E : Action.Payload> onAction(
        noinline handler: suspend context(AgentContext) InteractionScope<S>.(event: Action<E>) -> Unit,
    ) = onAction(E::class, handler)

    fun onExecute(handler: suspend context(AgentContext) InteractionScope<S>.(input: Action<P>) -> Unit) {
        executeHandler = handler
    }
}
