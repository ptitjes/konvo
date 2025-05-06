package io.github.ptitjes.konvo.frontend.discord.toolkit

import dev.kord.core.behavior.interaction.response.*
import dev.kord.core.entity.interaction.followup.*
import dev.kord.rest.builder.message.*

suspend fun <S> EphemeralMessageInteractionResponseBehavior.ephemeralWizard(
    initialState: S,
    contentBuilder: suspend WizardMessageBuilder<S>.(state: S) -> Unit,
) {
    var state = initialState
    var followupMessage: EphemeralFollowupMessage? = null
    var handlers: EphemeralMessageHandlers? = null

    fun unregisterHandlers() {
        handlers?.unregisterHandlers()
    }

    suspend fun update() {
        fun MessageBuilder.wizardBuilder(): WizardMessageBuilder<S> {
            handlers = EphemeralMessageHandlers(kord) { event ->
                event.interaction.message.id == followupMessage?.id
            }
            return WizardMessageBuilder(handlers, this) { newState ->
                unregisterHandlers()
                state = newState
                update()
            }
        }

        if (followupMessage == null) {
            followupMessage = createEphemeralFollowup { wizardBuilder().contentBuilder(state) }
        } else {
            edit { wizardBuilder().contentBuilder(state) }
        }
    }

    update()
}

class WizardMessageBuilder<S>(
    handlers: EphemeralMessageHandlers,
    delegate: MessageBuilder,
    private val doUpdateState: suspend (newState: S) -> Unit,
) : EphemeralMessageBuilder(handlers, delegate) {

    suspend fun updateState(newState: S) {
        doUpdateState(newState)
    }
}
