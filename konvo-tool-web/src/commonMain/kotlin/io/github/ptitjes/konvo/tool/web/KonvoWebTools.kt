package io.github.ptitjes.konvo.tool.web

import com.xemantic.ai.tool.schema.generator.*
import io.github.ptitjes.konvo.tool.web.utils.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.modelcontextprotocol.kotlin.sdk.server.*
import kotlinx.serialization.json.*

fun Server.addKonvoWebTools() {

    val client = HttpClient(CIO) {
        expectSuccess = true
        install(UserAgent) {
            agent = "konvo"
        }
    }
    val converter = HtmlToMarkdown()

    val duckDuckGo = DuckDuckGoEngine(client, converter)

    addJsonTool(
        name = "web_search",
        description = """
            Search the web for web pages that match the given query.
            Use this function if you need to find or search for a Web page.
            The search results are returned in JSON format with the following schema:
            ${Json.encodeToString(jsonSchemaOf<List<DuckDuckGoEngine.SearchResult>>())}
            You can actually use the `url` property of a returned search result as input to the `web_fetch` tool.
        """.trimIndent(),
        handler = duckDuckGo::search,
    )

    val fetcher = Fetcher(client, converter)

    addStringTool(
        name = "web_fetch",
        description = """
            Fetch a web page by its given url.
            Use this function if you need to fetch or get a specific web page.
        """.trimIndent(),
        handler = fetcher::fetch,
    )

    val wikipedia = Wikipedia(client)

    addJsonTool(
        name = "wikipedia_search",
        description = """
            Search Wikipedia for pages that match the given query.
            Use this function if you need to find or search for a Wikipedia page.
            The search results are returned in JSON format with the following schema:
            ${Json.encodeToString(Wikipedia.searchOutputSchema)}
            You can actually use the `key` property of a returned search result as input to the `wikipedia_get_page` tool.  
        """.trimIndent(),
        handler = wikipedia::search,
    )

    addJsonTool(
        name = "wikipedia_fetch_page",
        description = """
            Fetch the content of a Wikipedia page by the given key.
            Use this function if you need to fetch or get a specific Wikipedia page.
            A Wikipedia page key is a list of words separated by underscore (`_`) characters.
            You can find page keys in the result of a `wikipedia_search` tool call.
        """.trimIndent(),
        handler = wikipedia::getPage,
    )
}
