package io.github.ptitjes.konvo.plugin.core.conversations

import io.github.ptitjes.konvo.plugin.core.conversations.model.*
import io.github.ptitjes.syrup.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.modules.*

interface ActionRegistry {
    val serializersModule: SerializersModule
}

internal class DefaultActionRegistry(pluginContext: PluginContext) : ActionRegistry {

    private val actions by pluginContext.sourcedContributions(Actions)

    override val serializersModule by lazy {
        SerializersModule {
            actions.forEach { (source, contribution) ->
                contribution.klass.simpleName ?: error("Contribution class cannot be anonymous")
                addAction(source, contribution)
            }
        }
    }
}

@OptIn(InternalSerializationApi::class)
private fun <P : Action.Payload> SerializersModuleBuilder.addAction(
    source: Plugin,
    contribution: Actions.Contribution<P>,
) {
    polymorphic(
        baseClass = Action.Payload::class,
        actualClass = contribution.klass,
        actualSerializer = SerializerWrapper(
            serialPrefix = source.id.value,
            delegate = contribution.klass.serializer(),
        ),
    )
}

private class SerializerWrapper<T : Any>(
    private val serialPrefix: String,
    private val delegate: KSerializer<T>,
) : KSerializer<T> by delegate {
    override val descriptor: SerialDescriptor
        get() = SerialDescriptor(
            serialName = "$serialPrefix/${delegate.descriptor.serialName}",
            original = delegate.descriptor,
        )
}
