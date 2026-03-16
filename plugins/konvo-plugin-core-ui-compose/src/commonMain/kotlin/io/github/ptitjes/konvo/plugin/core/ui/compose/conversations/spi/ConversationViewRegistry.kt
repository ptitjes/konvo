package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi

class ConversationViewRegistry private constructor(
    private val containerPerSlot: Map<ConversationView.Slot<*, *>, Any?>,
) {
    @Suppress("UNCHECKED_CAST")
    operator fun <C> get(slot: ConversationView.Slot<*, C>): C =
        containerPerSlot[slot] as C? ?: error("No component registered for slot $slot")

    class Builder {
        fun contributeComponents(contribution: ConversationViews.Contribution) {
            val scope = ContributionScopeImpl(this)
            with(contribution) { scope.contribute() }
        }

        fun build(): ConversationViewRegistry = ConversationViewRegistry(
            containerPerSlot = handlerPerSlot.mapValues { (_, handler) -> handler.build() }
        )

        @Suppress("UNCHECKED_CAST")
        private fun <B, C> handlerFor(slot: ConversationView.Slot<B, C>): SlotHandler<B, C> =
            handlerPerSlot.getOrPut(slot) { SlotHandler(slot) } as SlotHandler<B, C>

        private val handlerPerSlot = mutableMapOf<ConversationView.Slot<*, *>, SlotHandler<*, *>>()

        private class SlotHandler<Builder, out Container>(
            private val slot: ConversationView.Slot<Builder, Container>,
        ) {
            val builder: Builder = slot.createBuilder()

            fun build(): Container = with(slot) { builder.build() }
        }

        private class ContributionScopeImpl(
            private val registryBuilder: Builder,
        ) : ConversationViews.ContributionScope() {

            override fun <Builder> ConversationView.Slot<Builder, *>.invoke(
                contribute: Builder.() -> Unit,
            ) {
                val slotHandler = registryBuilder.handlerFor(this)
                slotHandler.builder.contribute()
            }
        }
    }
}
