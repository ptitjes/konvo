package io.github.ptitjes.konvo.mcp.web

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.schema.generator.json.*
import kotlinx.serialization.*

class Wikipedia(
    private val client: HttpClient,
    private val baseUrl: String = "https://en.wikipedia.org/w/",
    private val pathPrefix: String = "rest.php/v1/",
) {
    @Serializable
    data class SearchRequest(
        @SerialDescription("The query to search for. It should only contain the search term and be relatively short.")
        val query: String,
        @SerialDescription("The maximum number of search results to query. Defaults to ${DEFAULT_LIMIT}.")
        val limit: Int? = null,
    )

    @Serializable
    data class SearchResponse(
        @SerialDescription("The list of search results.")
        val pages: List<MatchingPage>,
    )

    @Serializable
    data class MatchingPage(
        @SerialDescription("The id of the page.")
        val id: Int,
        @SerialDescription("The key of the page.")
        val key: String,
        @SerialDescription("The title of the page.")
        val title: String,
        @SerialDescription("The excerpt of the page.")
        val excerpt: String,
        @SerialDescription("The description of the page.")
        val description: String,
    )

    suspend fun search(request: SearchRequest): SearchResponse {
        return request("search/page") {
            append("q", request.query)
            append("limit", (request.limit ?: DEFAULT_LIMIT).toString())
        }
    }

    @Serializable
    data class GetPageRequest(
        @SerialDescription("The key of the page to fetch.")
        val key: String,
    )

    @Serializable
    data class GetPageResponse(
        @SerialDescription("The id of the page.")
        val id: Int,
        @SerialDescription("The key of the page.")
        val key: String,
        @SerialDescription("The title of the page.")
        val title: String,
        @SerialDescription("The content of the page.")
        val source: String,
    )

    suspend fun getPage(request: GetPageRequest): GetPageResponse {
        return request("page/${request.key}")
    }

    private suspend inline fun <reified T> request(
        path: String,
        crossinline parametersBuilder: ParametersBuilder.() -> Unit = {},
    ): T {
        val response = client.get("$baseUrl$pathPrefix$path") {
            url { parameters.parametersBuilder() }
        }
        return response.body()
    }

    companion object {
        const val DEFAULT_LIMIT = 5
    }
}
