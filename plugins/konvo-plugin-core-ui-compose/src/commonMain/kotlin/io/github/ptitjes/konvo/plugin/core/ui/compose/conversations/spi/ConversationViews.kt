package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.spi

import kotlin.reflect.*

sealed interface ConversationViews {

    fun interface Contribution {
        fun ContributionScope.contribute()
    }

    @DslMarker
    annotation class Marker

    @Marker
    abstract class ContributionScope {
        abstract operator fun <Builder> ConversationView.Slot<Builder, *>.invoke(contribute: Builder.() -> Unit)
    }

    @Marker
    abstract class BagScope<Scope, State, Control> {
        abstract fun add(view: ConversationView.View<Scope, State, Control>)
    }

    @Marker
    abstract class TaggedScope<Scope, State, Control, Tag> {
        abstract fun add(tag: Tag, view: ConversationView.View<Scope, State, Control>)
    }

    @Marker
    abstract class KeyedScope<Scope, State, Control, Key : Any> {
        abstract fun put(key: Key, view: ConversationView.View<Scope, State, Control>)
    }

    @Marker
    abstract class TypedScope<Scope, State : Any, Control> {
        abstract fun <T : State> put(klass: KClass<out T>, view: ConversationView.View<Scope, T, Control>)

        inline fun <reified T : State> put(view: ConversationView.View<Scope, T, Control>) = put(klass = T::class, view = view)
    }
}
