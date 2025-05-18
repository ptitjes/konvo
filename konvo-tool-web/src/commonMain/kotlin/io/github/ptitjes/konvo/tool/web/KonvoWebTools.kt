package io.github.ptitjes.konvo.tool.web

import io.github.ptitjes.konvo.tool.web.utils.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.UserAgent
import io.modelcontextprotocol.kotlin.sdk.*
import io.modelcontextprotocol.kotlin.sdk.server.*
import kotlinx.serialization.json.*

fun Server.addKonvoWebTools() {
    val client = HttpClient(CIO) {
        expectSuccess = true
        install(UserAgent) {
            agent = "konvo"
        }
    }

    val wikipedia = Wikipedia(client)

    addTool(
        name = "wikipedia_search",
        description = """
            Search for a Wikipedia page.
            The search results are returned in JSON format with the following schema:
            ${Json.encodeToString(Wikipedia.searchOutputSchema)}
            You can actually use the `key` property of a returned search result as input to the `wikipedia_get_page` tool.  
        """.trimIndent(),
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                put("query", buildJsonObject {
                    put("type", "string")
                    put(
                        "description",
                        "The query to search for. It should only contain the search term and be relatively short."
                    )
                })
                put("limit", buildJsonObject {
                    put("type", "number")
                    put(
                        "description",
                        "The maximum number of search results to query. Defaults to ${Wikipedia.DEFAULT_LIMIT}."
                    )
                })
            },
            required = listOf("query")
        )
    ) { request ->
        val query = request.arguments["query"]!!.jsonPrimitive.content
        val limit = request.arguments["limit"]?.jsonPrimitive?.int

        CallToolResult(
            content = listOf(JsonContent(wikipedia.search(query, limit)))
        )
    }

    addTool(
        name = "wikipedia_get_page",
        description = """
            Fetch the content of a Wikipedia page by the given key.
            A Wikipedia page key is a list of words separated by underscore (`_`) characters.
            You can find page keys in the result of a `wikipedia_search` tool call.
        """.trimIndent(),
        inputSchema = Tool.Input(
            properties = buildJsonObject {
                put("key", buildJsonObject {
                    put("type", "string")
                    put("description", "The key of the page to fetch.")
                })
            },
            required = listOf("key")
        )
    ) { request ->
        val key = request.arguments["key"]!!.jsonPrimitive.content

        CallToolResult(
            content = listOf(JsonContent(wikipedia.getPage(key)))
        )
    }
}
