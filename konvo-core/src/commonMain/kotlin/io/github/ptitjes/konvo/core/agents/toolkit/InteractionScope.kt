package io.github.ptitjes.konvo.core.agents.toolkit

import io.github.ptitjes.konvo.core.conversations.model.*

interface InteractionScope<S> {
    val state: S
    fun updateState(state: S)
    fun updateState(updater: (previous: S) -> S)

    suspend fun <P : Action.Agent> act(payload: P): Action<P>

    suspend fun <P : Action.Payload, S2> runInteraction(driver: InteractionDriver<P, S2>, trigger: Action<P>): S2

    suspend fun leaveInteraction()
}
