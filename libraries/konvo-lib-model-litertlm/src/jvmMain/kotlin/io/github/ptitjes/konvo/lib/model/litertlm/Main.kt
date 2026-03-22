package io.github.ptitjes.konvo.lib.model.litertlm

import ai.koog.agents.core.tools.*
import ai.koog.prompt.dsl.*
import ai.koog.prompt.markdown.*
import ai.koog.prompt.message.Message
import com.google.ai.edge.litertlm.*
import kotlinx.datetime.*
import kotlinx.datetime.format.*
import kotlinx.io.files.*
import kotlin.time.Clock

suspend fun main() {
    val engineCacheDirectory = Path("/tmp/litertlm-cache/")
    val modelsDirectory = Path("/home/didier/Documents/LiteRT-ML")
    val modelPathById = SystemFileSystem.list(modelsDirectory)
        .filter { it.name.endsWith(".litertlm") }
        .associate { it.name.removeSuffix(".litertlm") to it.toString() }

    val settings = LiteRTLMClientSettings(
        modelPathById = modelPathById,
        engineCacheDirectory = engineCacheDirectory.toString(),
        backend = Backend.CPU(2),
    )

    val client = LiteRTLMClient(settings = settings)

    var toolCalls = 0
    repeat(100) { iteration ->
        val areToolCalls = client.runPrompt()
        if (areToolCalls) toolCalls++

        println("Iteration ${iteration + 1}: Tool call count: $toolCalls")
    }
}

private suspend fun LiteRTLMClient.runPrompt(): Boolean {
    val dateString = Clock.System.now()
        .toLocalDateTime(TimeZone.currentSystemDefault())
        .date.format(dateFormat)

    val prompt = prompt("test") {
        system {
            markdown {
                +"You are a helpful assistant, that thrives at answering the user's questions."
                br()

                bold("IMPORTANT: USE TOOLS TO GET ACCURATE AND UP-TO-DATE INFORMATION.")

                h1("Output Format")
                bulleted {
                    item("Use formal language.")
                    item("Keep responses concise and engaging unless the situation demands elaboration.")
                    item("If you use Markdown syntax, ensure the syntax is valid.")
//                    item("Provide your sources.")
                }
                br()

                +"Today Date: $dateString"
            }
        }
        user { +"Give me recent romance movie recommendations." }
    }

    val responses = this.execute(
        prompt = prompt,
        model = LiteRTLMModels.AGENT_GEMMA,
        tools = tools,
    )

    return responses.all { it is Message.Tool.Call }
}

private val dateFormat = LocalDate.Format {
    day()
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}

private val tools = listOf(
    ToolDescriptor(
        name = "web_search",
        description = """
                Search the web for web pages that match the given query.
                Use this function if you need to find or search for a Web page.
                A search result only contains a snippet of the page and is not accurate to answer the user.
                You can actually use the `url` property of a returned search result as input to the `web_fetch` tool
                and retrieve the actual content of the page.
            """.trimIndent(),
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "query",
                description = "The query to search for. It should only contain the search term and be relatively short.",
                type = ToolParameterType.String,
            )
        )
    ),
    ToolDescriptor(
        name = "web_fetch",
        description = """
            Fetch a web page by its given url.
            Use this function if you need to fetch or get a specific web page.
        """.trimIndent(),
        requiredParameters = listOf(
            ToolParameterDescriptor(
                name = "url",
                description = "The url of the page to fetch.",
                type = ToolParameterType.String,
            )
        )
    ),
)
