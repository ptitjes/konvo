package io.github.ptitjes.konvo.core.agents.toolkit

import ai.koog.agents.core.agent.*
import ai.koog.agents.core.agent.context.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.model.*
import ai.koog.prompt.llm.*
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.core.agents.*
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.*
import kotlin.concurrent.atomics.*
import kotlin.reflect.*
import kotlin.reflect.full.*

fun WorkflowBuilder<*, *>.testWorkflow(
    name: String? = null,
    request: Node.Port<String>,
) = workflow(name, request) { request ->
    val isLongRequest by conditional(input = request) { it.length > 10 }

    val trueBranch by workflow(input = isLongRequest.onTrue) { input ->
        val runRequest by runRequest(input = input) {
            user("Hello")
        }

        runRequest.responses
    }

    val falseBranch by workflow(input = isLongRequest.onFalse) { input ->
        val runRequest by runRequest(input = input) {
            user("Hello")
        }

        runRequest.responses
    }

    val mergeResults by merge(input1 = trueBranch.output, input2 = falseBranch.output)

    mergeResults.output
}

suspend context(_: AgentContext)
inline fun <reified I, reified O> InteractionScope<*>.runWorkflow(
    promptExecutor: PromptExecutor,
    model: LLModel,
    maxAgentIterations: Int,
    input: I,
    workflowExecutor: WorkflowExecutor<I, O>,
): O {
    val agent = buildAgent<I, O>(
        promptExecutor = promptExecutor,
        model = model,
        maxAgentIterations = maxAgentIterations,
        strategy = functionalStrategy("workflow") { input ->
            with(workflowExecutor) { execute(input) }
        },
    )

    return agent.run(input)
}

@OptIn(ExperimentalAtomicApi::class)
class WorkflowExecutor<I, O> internal constructor(
    private val workflowData: WorkflowData<I, O>,
) {
    suspend fun AIAgentContext.execute(input: I): O {
        class Context(
            // parent: WorkflowContext, ????
        ) : WorkflowContext, AIAgentContext by this@execute {
            // Have two types of port? (single and flow)
            // Or make all ports be flows?
            private val state = AtomicReference<Map<Node.Port<*>, CompletableDeferred<Any>>>(mapOf())

            private fun <T> deferredFor(port: Node.Port<T>): CompletableDeferred<T> {
                val map = state.updateAndFetch { previous ->
                    val deferred = previous[port] ?: CompletableDeferred()
                    previous + (port to deferred)
                }

                @Suppress("UNCHECKED_CAST") return map[port] as CompletableDeferred<T>
            }

            override suspend fun <T> get(port: Node.Port<T>): T {
                return deferredFor(port).await()
            }

            override suspend fun <T> set(port: Node.Port<T>, value: T) {
                deferredFor(port).complete(value)
            }
        }

        val context = Context()

        context[workflowData.input] = input

        workflowData.nodes.forEach { data ->
            data.execute.invoke(context, data.node)
        }

        return context[workflowData.output]
    }
}

@WorkflowAgentDsl
fun <I, O> WorkflowBuilder<*, *>.workflow(
    name: String? = null,
    input: Node.Port<I>,
    builderAction: WorkflowBuilder<I, O>.(input: Node.Port<I>) -> Node.Port<O>,
): WorkflowBuilder<*, *>.NodeDelegate<Workflow<I, O>> {
    val builder = WorkflowBuilder<I, O>()
    val output = builder.builderAction(input)
    val workflowData = WorkflowData(input, output, builder.build())
    val executor = WorkflowExecutor(workflowData)

    return node<Workflow<I, O>>(name) {
        val context = contextOf<WorkflowContext>()
        val input = context[workflowData.input]
        with(executor) { context.execute(input) }
    }
}

open class Workflow<I, O> : Node {
    open val input: Node.Port<I> by port()
    open val output: Node.Port<O> by port()
}

internal class WorkflowData<I, O> internal constructor(
    override val input: Node.Port<I>,
    override val output: Node.Port<O>,
    internal val nodes: List<NodeData>,
) : Workflow<I, O>() {
    internal data class NodeData(
        val name: String,
        val node: Node,
        val execute: suspend context(WorkflowContext) Node.() -> Unit,
    )
}

interface WorkflowContext : AIAgentContext {
    suspend operator fun <T> get(port: Node.Port<T>): T
    suspend operator fun <T> set(port: Node.Port<T>, value: T)
}

interface Node {
    class Port<@Suppress("unused") out T> internal constructor(val name: String)

    fun <T> port(name: String? = null): PortDelegate<T> = PortDelegate(name)

    class PortDelegate<out T> internal constructor(private val name: String?) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): Port<T> {
            return Port(name ?: property.name)
        }
    }
}

@DslMarker
annotation class WorkflowAgentDsl

@WorkflowAgentDsl
class WorkflowBuilder<I, O> {
    private val nodes = mutableListOf<WorkflowData.NodeData>()

    internal fun build(): List<WorkflowData.NodeData> = nodes.toList()

    inline fun <reified N : Node> node(
        name: String? = null,
        noinline execute: suspend context(WorkflowContext) N.() -> Unit,
    ): NodeDelegate<N> = node(name, N::class::createInstance, execute)

    fun <N : Node> node(
        name: String? = null,
        build: () -> N,
        execute: suspend context(WorkflowContext) N.() -> Unit,
    ): NodeDelegate<N> = NodeDelegate(name, build, execute)

    inner class NodeDelegate<N : Node>(
        private val name: String?,
        private val build: () -> N,
        private val execute: suspend context(WorkflowContext) N.() -> Unit,
    ) {
        operator fun getValue(thisRef: Any?, property: KProperty<*>): N {
            val node = build()

            @Suppress("UNCHECKED_CAST") nodes += WorkflowData.NodeData(
                name = name ?: property.name,
                node = node,
                execute = execute as suspend context(WorkflowContext) Node.() -> Unit,
            )

            return node
        }
    }
}

fun <T, R> Node.Port<T>.transformed(
    transform: suspend context(WorkflowContext) (T) -> R,
): Node.Port<R> =
    TODO()

//open class SingleOutputNode<O> : Node, Node.Port<O> {
//}

context(context: WorkflowContext)
suspend fun <T> Node.Port<T>.get(): T = context[this]

context(context: WorkflowContext)
suspend inline fun <T> Node.Port<T>.whenSet(
    crossinline block: suspend (T) -> Unit,
) = block(context[this])

context(context: WorkflowContext)
suspend fun <T> Node.Port<T>.emit(value: T) {
    context[this] = value
}

@WorkflowAgentDsl
fun <T, R> WorkflowBuilder<*, *>.convert(
    name: String? = null,
    input: Node.Port<T>,
    mapper: (T) -> R,
) = node<Convert<T, R>>(name) {
    val inputValue = input.get()
    output.emit(mapper(inputValue))
}

class Convert<T, R> : Node {
    val output by port<R>()
}

@WorkflowAgentDsl
fun <T> WorkflowBuilder<*, *>.merge(
    name: String? = null,
    input1: Node.Port<T>,
    input2: Node.Port<T>,
) = node<Merge<T>>(name) {
    coroutineScope {
        val deferredValue1 = async { input1.get() }
        val deferredValue2 = async { input2.get() }

        select {
            deferredValue1.onAwait { value -> output.emit(value) }
            deferredValue2.onAwait { value -> output.emit(value) }
        }
    }
}

class Merge<T> : Node {
    val output by port<T>()
}

@WorkflowAgentDsl
fun <T> WorkflowBuilder<*, *>.conditional(
    name: String? = null,
    input: Node.Port<T>,
    predicate: (T) -> Boolean,
) = node<Conditional<T>>(name) {
    val inputValue = input.get()
    if (predicate(inputValue)) onTrue.emit(inputValue) else onFalse.emit(inputValue)
}

class Conditional<T> : Node {
    val onTrue by port<T>()
    val onFalse by port<T>()
}

@WorkflowAgentDsl
fun <T> WorkflowBuilder<*, *>.runRequest(
    name: String? = null,
    input: Node.Port<T>,
    requestBuilder: PromptBuilder.(T) -> Unit,
) = node<RunRequest>(name) {
    input.whenSet { input ->
        val context = contextOf<AIAgentContext>()
        responses.emit(context.llm.writeSession {
            appendPrompt { requestBuilder(input) }
            requestLLMMultiple()
        })
    }
}

class RunRequest : Node {
    val responses by port<List<Message.Response>>()
}
