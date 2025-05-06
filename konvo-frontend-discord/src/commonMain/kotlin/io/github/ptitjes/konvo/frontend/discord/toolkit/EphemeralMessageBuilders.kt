@file:OptIn(ExperimentalContracts::class)

package io.github.ptitjes.konvo.frontend.discord.toolkit

import dev.kord.common.*
import dev.kord.common.annotation.*
import dev.kord.common.entity.*
import dev.kord.core.*
import dev.kord.core.behavior.*
import dev.kord.core.behavior.channel.*
import dev.kord.core.behavior.interaction.*
import dev.kord.core.entity.*
import dev.kord.core.entity.interaction.*
import dev.kord.core.event.interaction.*
import dev.kord.rest.builder.component.*
import dev.kord.rest.builder.interaction.*
import dev.kord.rest.builder.message.*
import kotlinx.coroutines.*
import kotlin.contracts.*
import kotlin.contracts.InvocationKind.*
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.*

suspend fun MessageChannelBehavior.createEphemeralMessage(
    contentBuilder: EphemeralCreateMessageBuilder.() -> Unit,
) {
    var message: Message? = null
    var handlers: EphemeralMessageHandlers? = null

    fun unregisterHandlers() {
        handlers?.unregisterHandlers()
    }

    suspend fun createOrUpdate() {
        fun MessageBuilder.makeBuilder(): EphemeralCreateMessageBuilder {
            handlers = EphemeralMessageHandlers(kord) { event ->
                event.interaction.message.id == message?.id
            }
            return EphemeralCreateMessageBuilder(
                handlers = handlers,
                delegate = this,
                doUpdate = {
                    unregisterHandlers()
                    createOrUpdate()
                },
                doDelete = {
                    unregisterHandlers()
                    message?.delete()
                }
            )
        }

        if (message == null) {
            message = createMessage {
                flags = (flags ?: MessageFlags()) + MessageFlag.IsComponentsV2
                makeBuilder().contentBuilder()
            }
        } else {
            message.edit {
                makeBuilder().contentBuilder()
            }
        }
    }

    createOrUpdate()
}

class EphemeralCreateMessageBuilder(
    handlers: EphemeralMessageHandlers,
    delegate: MessageBuilder,
    private val doUpdate: suspend () -> Unit,
    private val doDelete: suspend () -> Unit,
) : EphemeralMessageBuilder(handlers, delegate) {

    suspend fun update() {
        doUpdate()
    }

    suspend fun delete() {
        doDelete()
    }
}

class EphemeralMessageHandlers(
    private val kord: Kord,
    private val eventMessageFilter: (event: ComponentInteractionCreateEvent) -> Boolean,
) {
    private val registeredHandlers: MutableList<Job> = mutableListOf()

    fun registerButtonHandler(block: suspend HandlerScope<ButtonInteraction>.() -> Unit): String {
        val id = newHandlerId()

        registeredHandlers += kord.on<ButtonInteractionCreateEvent> {
            if (!eventMessageFilter(this)) return@on
            if (interaction.componentId != id) return@on

            HandlerScope(kord, interaction).block()
        }

        return id
    }

    fun registerSelectMenuHandler(block: suspend HandlerScope<SelectMenuInteraction>.() -> Unit): String {
        val id = newHandlerId()

        registeredHandlers += kord.on<SelectMenuInteractionCreateEvent> {
            if (!eventMessageFilter(this)) return@on
            if (interaction.componentId != id) return@on

            HandlerScope(kord, interaction).block()
        }

        return id
    }

    fun unregisterHandlers() {
        registeredHandlers.forEach { it.cancel() }
    }
}

class HandlerScope<I : ComponentInteraction> internal constructor(
    private val kord: Kord,
    val interaction: I,
) {
    suspend fun acknowledge() {
        interaction.deferEphemeralMessageUpdate()
    }

    suspend fun acknowledgeWithModal(title: String, builder: ModalBuilder.() -> Unit): Map<String, String?>? =
        withTimeoutOrNull(15.minutes) {
            val id = newHandlerId()
            val deferredResult = CompletableDeferred<Map<String, String?>>()

            val handler = kord.on<ModalSubmitInteractionCreateEvent> {
                if (interaction.modalId != id) return@on

                @OptIn(KordUnsafe::class)
                interaction.deferEphemeralMessageUpdate()

                deferredResult.complete(interaction.textInputs.mapValues { (_, input) -> input.value })
            }

            interaction.modal(title = title, id, builder = builder)

            val result = deferredResult.await()

            handler.cancel()

            result
        }
}

@OptIn(ExperimentalUuidApi::class)
private fun newHandlerId(): String = Uuid.random().toString()

@KordDsl
open class EphemeralMessageBuilder(
    private val handlers: EphemeralMessageHandlers,
    private val delegate: MessageBuilder,
) : EphemeralComponentContainerBuilder(handlers, delegate), MessageBuilder by delegate {
    fun clear() {
        delegate.content = null
        delegate.embeds = mutableListOf()
        delegate.components = mutableListOf()
    }

    /**
     * Adds an [container][ContainerBuilder] configured by the [builder] to the [components][MessageBuilder.components] of
     * the message.
     *
     * A message can have up to ten top-level components.
     */
    fun container(builder: EphemeralContainerBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val handlers = handlers
        delegate.container { EphemeralContainerBuilder(handlers, this).builder() }

        delegate.flags = (delegate.flags ?: MessageFlags()) + MessageFlag.IsComponentsV2
    }
}

class EphemeralContainerBuilder(
    handlers: EphemeralMessageHandlers,
    private val delegate: ContainerBuilder,
) : EphemeralComponentContainerBuilder(handlers, delegate) {

    /**
     * Whether the component is a spoiler. Defaults to `false`.
     */
    var spoiler: Boolean? by delegate::spoiler

    /**
     * The accent color of the container.
     *
     * If provided, the container will be rendered with a left-hand border of the specified color.
     */
    var accentColor: Color? by delegate::accentColor

    /**
     * Adds a [text].
     */
    fun textDisplay(text: String): Unit = textDisplay {
        content = text
    }
}

/**
 * A builder for an object which can contain [multiple components][ContainerComponentBuilder].
 *
 * @see MessageBuilder
 * @see ContainerBuilder
 */
open class EphemeralComponentContainerBuilder(
    private val handlers: EphemeralMessageHandlers,
    private val delegate: ComponentContainerBuilder,
) {
    fun actionRow(builder: EphemeralActionRowBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }

        val handlers = handlers
        delegate.actionRow { EphemeralActionRowBuilder(handlers, this).builder() }
    }

    fun section(builder: EphemeralSectionBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val handlers = handlers
        delegate.section { EphemeralSectionBuilder(handlers, this).builder() }
    }

    fun textDisplay(builder: TextDisplayBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        delegate.textDisplay(builder)
    }

    fun mediaGallery(builder: MediaGalleryBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        delegate.mediaGallery(builder)
    }

    fun separator(builder: SeparatorBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        delegate.separator(builder)
    }

    fun file(builder: FileBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        delegate.file(builder)
    }
}

@KordDsl
class EphemeralActionRowBuilder(
    private val handlers: EphemeralMessageHandlers,
    private val delegate: ActionRowBuilder,
) {
    fun interactionButton(
        style: ButtonStyle,
        onClick: suspend HandlerScope<ButtonInteraction>.() -> Unit = {},
        builder: ButtonBuilder.InteractionButtonBuilder.() -> Unit,
    ) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val id = handlers.registerButtonHandler(onClick)
        delegate.interactionButton(style, id, builder)
    }

    fun linkButton(url: String, builder: ButtonBuilder.LinkButtonBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        delegate.linkButton(url, builder)
    }

    /**
     * Creates and adds a string select menu with the [customId] and configured by the [builder].
     * An ActionRow with a select menu cannot have any other select menus or buttons.
     */
    fun stringSelect(
        onSelect: suspend HandlerScope<SelectMenuInteraction>.(selected: List<String>) -> Unit = {},
        builder: StringSelectBuilder.() -> Unit,
    ) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val id = handlers.registerSelectMenuHandler { onSelect(interaction.values) }
        delegate.stringSelect(id, builder)
    }

    /**
     * Creates and adds a user select menu with the [customId] and configured by the [builder].
     * An ActionRow with a select menu cannot have any other select menus or buttons.
     */
    fun userSelect(
        onSelect: suspend HandlerScope<SelectMenuInteraction>.(selected: List<String>) -> Unit = {},
        builder: UserSelectBuilder.() -> Unit = {},
    ) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val id = handlers.registerSelectMenuHandler { onSelect(interaction.values) }
        delegate.userSelect(id, builder)
    }

    /**
     * Creates and adds a role select menu with the [customId] and configured by the [builder].
     * An ActionRow with a select menu cannot have any other select menus or buttons.
     */
    fun roleSelect(
        onSelect: suspend HandlerScope<SelectMenuInteraction>.(selected: List<String>) -> Unit = {},
        builder: RoleSelectBuilder.() -> Unit = {},
    ) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val id = handlers.registerSelectMenuHandler { onSelect(interaction.values) }
        delegate.roleSelect(id, builder)
    }

    /**
     * Creates and adds a mentionable select menu with the [customId] and configured by the [builder].
     * An ActionRow with a select menu cannot have any other select menus or buttons.
     */
    fun mentionableSelect(
        onSelect: suspend HandlerScope<SelectMenuInteraction>.(selected: List<String>) -> Unit = {},
        builder: MentionableSelectBuilder.() -> Unit = {},
    ) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val id = handlers.registerSelectMenuHandler { onSelect(interaction.values) }
        delegate.mentionableSelect(id, builder)
    }

    /**
     * Creates and adds a channel select menu with the [customId] and configured by the [builder].
     * An ActionRow with a select menu cannot have any other select menus or buttons.
     */
    fun channelSelect(
        onSelect: suspend HandlerScope<SelectMenuInteraction>.(selected: List<String>) -> Unit = {},
        builder: ChannelSelectBuilder.() -> Unit = {},
    ) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val id = handlers.registerSelectMenuHandler { onSelect(interaction.values) }
        delegate.channelSelect(id, builder)
    }
}

class EphemeralSectionBuilder(
    private val handlers: EphemeralMessageHandlers,
    private val delegate: SectionBuilder,
) {
    /**
     * Adds an interaction button as an accessory to this section.
     * This is mutually exclusive with other accessory components.
     *
     * @param style the style of this button, use [linkButtonAccessory] for [ButtonStyle.Link].
     * @param customId the ID of this button, used to identify component interactions.
     */
    fun interactionButtonAccessory(
        style: ButtonStyle,
        onClick: suspend HandlerScope<ButtonInteraction>.() -> Unit = {},
        builder: ButtonBuilder.() -> Unit = {},
    ) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        val id = handlers.registerButtonHandler(onClick)
        delegate.interactionButtonAccessory(style, id, builder)
    }

    /**
     * Adds a link button as an accessory to this section.
     * This is mutually exclusive with other accessory components.
     *
     * @param url The url to open.
     */
    fun linkButtonAccessory(url: String, builder: ButtonBuilder.() -> Unit = {}) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        delegate.linkButtonAccessory(url, builder)
    }

    /**
     * Adds a thumbnail as an accessory to this section.
     * This is mutually exclusive with other accessory components.
     */
    fun thumbnailAccessory(builder: ThumbnailBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        delegate.thumbnailAccessory(builder)
    }

    /**
     * Adds a text display as a component to this section.
     *
     *
     */
    fun textDisplay(builder: TextDisplayBuilder.() -> Unit) {
        contract { callsInPlace(builder, EXACTLY_ONCE) }
        delegate.textDisplay(builder)
    }

    fun textDisplay(content: String): Unit = textDisplay {
        this.content = content
    }
}
