package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.prompt.dsl.*
import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.core.agents.*
import io.github.ptitjes.konvo.core.conversations.*
import io.github.ptitjes.konvo.core.conversations.model.*
import io.github.ptitjes.konvo.core.conversations.model.events.*
import io.github.ptitjes.konvo.core.mcp.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.concurrent.atomics.*
import kotlin.coroutines.*
import kotlin.reflect.*
import ai.koog.prompt.message.Message as KoogMessage

abstract class InteractiveAgent<C : Any>(
    val configurationClass: KClass<C>,
    private val initialPrompt: () -> Prompt,
    private val mcpSessionFactory: ((coroutineContext: CoroutineContext) -> McpHostSession)? = null,
) : Agent {
    protected abstract val initialInteraction: InteractionDriver<ConversationControl.InviteAgent, *>

    @InteractiveAgentDsl
    fun <P : Action.Payload, S> interaction(
        protocol: InteractionProtocol,
        name: String? = null,
        builderAction: InteractionBuilder<P, Unit>.() -> Unit,
    ): InteractionDelegate<P, Unit> = interaction(
        protocol = protocol,
        name = name,
        initialState = { },
        builderAction = builderAction,
    )

    @InteractiveAgentDsl
    fun <P : Action.Payload, S> interaction(
        protocol: InteractionProtocol,
        name: String? = null,
        initialState: suspend context(AgentContext) (Action<P>) -> S,
        builderAction: InteractionBuilder<P, S>.() -> Unit,
    ): InteractionDelegate<P, S> = InteractionDelegate(name) {
        InteractionBuilder(protocol, initialState).apply { builderAction() }.build()
    }

    class InteractionDelegate<P : Action.Payload, S>(
        private val name: String?,
        private val block: (name: String) -> InteractionDriver<P, S>,
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): InteractionDriver<P, S> {
            return block(name ?: property.name)
        }
    }

    override suspend fun restoreSession(
        transcript: ConversationTranscript,
        invite: Action<ConversationControl.InviteAgent>,
        device: InteractionDevice.Agent,
    ): AgentSession = coroutineScope {
        val context = DefaultAgentContext(
            coroutineContext = coroutineContext,
            mcpSessionFactory = mcpSessionFactory,
            initialPrompt = initialPrompt,
        )

        context.updateState(AgentConfigurationStateKey, invite.payload.agentConfiguration)

        val session = DefaultAgentSession(
            context = context,
        )

        val conversationJustStarted = transcript.actions.none { it.sender == device.participant }

        val runner = InteractionRunner(
            coroutineContext = coroutineContext,
            context = context,
            driver = initialInteraction,
            device = device,
        )

        if (conversationJustStarted) {
            runner.execute(invite)
        } else {
            val pendingInteractions = mutableMapOf<String, Interaction>()
            val messages = mutableListOf<KoogMessage>()

            transcript.entries.forEach { entry ->
                when (entry) {
                    is InteractionBoundary.Start -> {
                        val interaction = entry.interaction
                        pendingInteractions[interaction.id] = entry.interaction
                    }

                    is InteractionBoundary.End -> {
                        val interaction = entry.interaction
                        pendingInteractions.remove(interaction.id)
                    }

                    is Action<*> -> {
                        when (entry.payload) {
                            is Messaging.Message -> {
                                val action = entry.asTypedAction<Messaging.Message>()
                                messages += action.toKoogMessage()
                            }
                        }
                    }
                }
            }

            check(pendingInteractions.size == 1) {
                "Expected exactly one pending interaction, got ${pendingInteractions.size}"
            }
            val interaction = pendingInteractions.values.last()

            context.appendToPrompt {
                messages(messages)
            }

            runner.recover(interaction, invite)
        }

        session
    }
}

private val logger = KotlinLogging.logger { }

private class InteractionRunner<P : Action.Payload, S>(
    coroutineContext: CoroutineContext,
    private val context: AgentContext,
    private val driver: InteractionDriver<P, S>,
    private val device: InteractionDevice.Agent,
    private val parent: InteractionRunner<*, *>? = null,
) {
    val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        logger.error(throwable) { "Running interaction ${interaction.id} failed" }
    }
    val coroutineScope = CoroutineScope(coroutineContext + exceptionHandler)

    private lateinit var job: Job
    private lateinit var interaction: Interaction

    @OptIn(ExperimentalAtomicApi::class)
    private lateinit var state: AtomicReference<S>

    suspend fun execute(triggerAction: Action<P>) {
        job = coroutineScope.launch {
            val interaction = device.startInteraction(
                protocol = driver.protocol,
                parent = parent?.interaction,
                trigger = triggerAction,
            )
            execute(interaction, triggerAction)
        }
    }

    suspend fun recover(interaction: Interaction, triggerAction: Action<P>) {
        job = coroutineScope.launch {
            execute(interaction, triggerAction)
        }
    }

    @OptIn(ExperimentalAtomicApi::class)
    suspend fun await(): S {
        job.join()
        return state.load()
    }

    @OptIn(ExperimentalAtomicApi::class)
    private suspend fun execute(interaction: Interaction, triggerAction: Action<P>): S = coroutineScope {
        this@InteractionRunner.interaction = interaction
        logger.debug { "Executing interaction ${interaction.id} (protocol: ${interaction.protocol.id})" }

        val initialState = driver.initialState(context, triggerAction)
        state = AtomicReference(initialState)

        val scope = object : InteractionScope<S> {
            override val state: S get() = this@InteractionRunner.state.load()

            override fun updateState(state: S) {
                this@InteractionRunner.state.exchange(state)
            }

            override fun updateState(updater: (previous: S) -> S) {
                this@InteractionRunner.state.update(updater)
            }

            override suspend fun <P2 : Action.Payload, S2> runInteraction(
                driver: InteractionDriver<P2, S2>,
                trigger: Action<P2>,
            ): S2 {
                // TODO use an AgentContext task management API
                val runner = InteractionRunner(
                    coroutineContext = coroutineScope.coroutineContext,
                    context = context,
                    driver = driver,
                    device = device,
                    parent = this@InteractionRunner,
                )
                runner.execute(trigger)
                return runner.await()
            }

            override suspend fun <P : Action.Agent> act(payload: P): Action<P> {
                // later, will send event with interaction id
                return device.act(payload)
            }

            override suspend fun leaveInteraction() {
                job.cancel()
                // Should we notify the parent? Or is cancellation enough?
            }
        }

        val eventListener = launch {
            logger.debug { "Listening to events for interaction $interaction" }
            device.actions
                .filter { it.sender != device.participant }
                // .filter { it.interactionId == id }
                .collect {
                    launch {
                        driver.onEvent[it.payload::class]?.invoke(context, scope, it)
                    }
                }
        }

        try {
            logger.debug { "Starting interaction ${interaction.id}" }
            driver.onEnter?.invoke(context, scope, triggerAction)
            logger.debug { "Before execution of interaction ${interaction.id}" }
            driver.onExecute?.invoke(context, scope, triggerAction) ?: awaitCancellation()
            logger.debug { "After execution of interaction ${interaction.id}" }
        } catch (e: Throwable) {
            logger.error(e) { "Error during execution of interaction ${interaction.id}" }
            driver.onError?.invoke(context, scope, e)
            throw e
        } finally {
            logger.debug { "Ending interaction ${interaction.id}" }
            eventListener.cancel()

            withContext(NonCancellable) {
                try {
                    driver.onLeave?.invoke(context, scope, triggerAction)
                } finally {
                    device.endInteraction(interaction)
                }
            }
        }

        return@coroutineScope state.load()
    }
}

/** Key for storing the [AgentConfiguration] in the agent state. */
private val AgentConfigurationStateKey: AgentStateKey<AgentConfiguration> =
    createAgentStateKey<AgentConfiguration>("konvo-interactive-agent-configuration")

@Suppress("UNCHECKED_CAST")
context(_: AgentContext)
val <C : Any> InteractiveAgent<C>.configuration: C
    get() =
        loadState<AgentConfiguration>(AgentConfigurationStateKey) as? C
            ?: throw IllegalStateException("Agent configuration not found in state")
