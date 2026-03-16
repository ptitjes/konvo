package io.github.ptitjes.konvo.plugin.core.agents.toolkit

import io.github.ptitjes.konvo.plugin.core.agents.*
import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import kotlin.reflect.*

class InteractionDriver<P : Action.Payload, S>(
    internal val protocol: InteractionProtocol,
    internal val initialState: suspend context(AgentContext) (Action<P>) -> S,
    internal val onEnter: Handler<P, S, Action<P>>? = null,
    internal val onError: Handler<P, S, Throwable>? = null,
    internal val onLeave: Handler<P, S, Action<P>>? = null,
    internal val onEvent: Map<KClass<out Action.Payload>, Handler<P, S, Action<*>>> = emptyMap(),
    internal val onExecute: Handler<P, S, Action<P>>? = null,
) {
    typealias Handler<P, S, T> = suspend context(AgentContext) InteractionScope<S>.(T) -> Unit
}
