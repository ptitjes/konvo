package io.github.ptitjes.konvo.frontend.compose.conversations.spi

import androidx.compose.runtime.*
import kotlin.reflect.*

class ConversationView {

    fun interface View<in Scope, in State, in Control> {
        @Composable
        context(control: Control)
        fun Scope.Content(viewState: State)
    }

    interface Slot<Builder, out Container> {
        fun createBuilder(): Builder
        fun Builder.build(): Container

        open class Bag<Scope, State, Control> :
            Slot<Bag.Builder<Scope, State, Control>, List<View<Scope, State, Control>>> {

            class Builder<Scope, State, Control> : ConversationViews.BagScope<Scope, State, Control>() {
                private val _contributions = mutableListOf<View<Scope, State, Control>>()
                val contributions: List<View<Scope, State, Control>> get() = _contributions.toList()

                override fun add(view: View<Scope, State, Control>) {
                    _contributions.add(view)
                }
            }

            override fun createBuilder() = Builder<Scope, State, Control>()

            override fun Builder<Scope, State, Control>.build() = contributions
        }

        open class Tagged<Scope, State, Control, Tag> :
            Slot<Tagged.Builder<Scope, State, Control, Tag>, List<Tagged.TaggedView<Scope, State, Control, Tag>>> {

            class Builder<Scope, State, Control, Tag> :
                ConversationViews.TaggedScope<Scope, State, Control, Tag>() {

                private val _contributions = mutableListOf<TaggedView<Scope, State, Control, Tag>>()
                val contributions: List<TaggedView<Scope, State, Control, Tag>> get() = _contributions.toList()

                override fun add(tag: Tag, view: View<Scope, State, Control>) {
                    _contributions.add(TaggedView(tag, view))
                }
            }

            class TaggedView<Scope, State, Control, Tag>(
                val tag: Tag,
                val view: View<Scope, State, Control>,
            )

            override fun createBuilder() = Builder<Scope, State, Control, Tag>()

            override fun Builder<Scope, State, Control, Tag>.build() = contributions
        }

        open class Keyed<Scope, State, Control, Key : Any> :
            Slot<Keyed.Builder<Scope, State, Control, Key>, Map<Key, View<Scope, State, Control>>> {

            class Builder<Scope, State, Control, Key : Any> :
                ConversationViews.KeyedScope<Scope, State, Control, Key>() {
                private val _contributions = mutableMapOf<Key, View<Scope, State, Control>>()
                val contributions: Map<Key, View<Scope, State, Control>> get() = _contributions.toMap()

                override fun put(key: Key, view: View<Scope, State, Control>) {
                    _contributions[key] = view
                }
            }

            override fun createBuilder() = Builder<Scope, State, Control, Key>()

            override fun Builder<Scope, State, Control, Key>.build() = contributions
        }

        open class Typed<Scope, State : Any, Control> :
            Slot<Typed.Builder<Scope, State, Control>, View<Scope, State, Control>> {

            class Builder<Scope, State : Any, Control> : ConversationViews.TypedScope<Scope, State, Control>() {
                private val _contributions = mutableMapOf<KClass<out State>, View<Scope, State, Control>>()
                val contributions: Map<KClass<out State>, View<Scope, State, Control>> get() = _contributions.toMap()

                override fun <T : State> put(klass: KClass<out T>, view: View<Scope, T, Control>) {
                    @Suppress("UNCHECKED_CAST")
                    register(klass, view as View<Scope, State, Control>)
                }

                private fun register(klass: KClass<out State>, view: View<Scope, State, Control>) {
                    _contributions[klass] = view

                    if (klass.isSealed) {
                        klass.sealedSubclasses.forEach { register(it, view) }
                    }
                }
            }

            override fun createBuilder() = Builder<Scope, State, Control>()

            override fun Builder<Scope, State, Control>.build() = View<Scope, State, Control> { viewState ->
                val typedView = contributions[viewState::class]
                    ?: error("No component registered for state class ${viewState::class}")
                with(typedView) { Content(viewState) }
            }
        }
    }
}
