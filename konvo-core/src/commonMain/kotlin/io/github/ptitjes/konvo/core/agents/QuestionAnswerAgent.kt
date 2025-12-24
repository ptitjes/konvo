package io.github.ptitjes.konvo.core.agents

import ai.koog.agents.core.dsl.builder.*
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.tools.annotations.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.llms.*
import ai.koog.prompt.markdown.*
import ai.koog.prompt.message.*
import io.github.ptitjes.konvo.core.agents.toolkit.*
import io.github.ptitjes.konvo.core.mcp.*
import io.github.ptitjes.konvo.core.models.*
import kotlinx.datetime.*
import kotlinx.datetime.format.*
import kotlin.coroutines.*
import kotlin.time.Clock

@OptIn(InternalAgentToolsApi::class)
fun buildQuestionAnswerAgent(
    model: ModelCard,
    mcpSessionFactory: (coroutineContext: CoroutineContext) -> McpHostSession,
    mcpServerNames: Set<String>,
): Agent {
    return DefaultAgent(
        systemPrompt = buildSystemPrompt(),
        model = model.toLLModel(),
        promptExecutor = CallFixingPromptExecutor(SingleLLMPromptExecutor(model.getLLMClient())),
        strategy = {
            strategy("qa") {
                val qa by qaWithTools()
                nodeStart then qa then nodeFinish
            }
        },
        mcpSessionFactory = mcpSessionFactory,
        mcpServerNames = mcpServerNames,
    )
}

private fun buildSystemPrompt(): Prompt {
    val dateString = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.format(dateFormat)

    return prompt("qa") {
        system {
            markdown {
                +"You are a helpful assistant, that thrives at answering the user's questions."
                br()

                h1("Output Format")
                bulleted {
                    item("Use formal language.")
                    item("Keep responses concise and engaging unless the situation demands elaboration.")
                    item("If you use Markdown syntax, ensure the syntax is valid.")
                }
                br()

                +"Today Date: $dateString"
            }
        }
    }
}

private val dateFormat = LocalDate.Format {
    day()
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}

private fun AIAgentSubgraphBuilderBase<*, *>.qaWithTools() = subgraph<Message.User, List<Message.Assistant>> {
    val dumpInitialRequest by dumpToPrompt()
    val initialRequest by requestLLM()
    val processResponses by nodeDoNothing<List<Message.Response>>()
    val vetToolCalls by nodeVetToolCalls()
    val executeTools by nodeExecuteVettedToolCalls(parallelTools = true)
    val toolResultsRequest by nodeLLMSendMultipleToolResults()

    edge(nodeStart forwardTo dumpInitialRequest)
    edge(dumpInitialRequest forwardTo initialRequest)
    edge(initialRequest forwardTo processResponses)

    edge(processResponses forwardTo vetToolCalls onMultipleToolCalls { true })
    edge(processResponses forwardTo nodeFinish onMultipleAssistantMessages { true })

    edge(vetToolCalls forwardTo executeTools)
    edge(executeTools forwardTo toolResultsRequest)
    edge(toolResultsRequest forwardTo processResponses)
}
