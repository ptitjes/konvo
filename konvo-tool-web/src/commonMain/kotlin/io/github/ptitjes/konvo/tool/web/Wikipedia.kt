package io.github.ptitjes.konvo.tool.web

import com.xemantic.ai.tool.schema.meta.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class Wikipedia(
    private val client: HttpClient,
    private val baseUrl: String = "https://en.wikipedia.org/w/",
    private val pathPrefix: String = "rest.php/v1/",
) {
    @Serializable
    data class SearchRequest(
        @Description("The query to search for. It should only contain the search term and be relatively short.")
        val query: String,
        @Description("The maximum number of search results to query. Defaults to ${DEFAULT_LIMIT}.")
        val limit: Int? = null,
    )

    suspend fun search(request: SearchRequest): JsonObject {
        return request("search/page") {
            append("q", request.query)
            append("limit", (request.limit ?: DEFAULT_LIMIT).toString())
        }
    }

    @Serializable
    data class GetPageRequest(
        @Description("The key of the page to fetch.")
        val key: String,
    )

    suspend fun getPage(request: GetPageRequest): JsonObject {
        return request("page/${request.key}")
    }

    private suspend fun request(path: String, parametersBuilder: ParametersBuilder.() -> Unit = {}): JsonObject {
        val response = client.get("$baseUrl$pathPrefix$path") {
            url { parameters.parametersBuilder() }
        }
        return Json.decodeFromString(response.bodyAsText())
    }

    companion object {
        const val DEFAULT_LIMIT = 5

        val searchOutputSchema = Json.encodeToString(
            Json.decodeFromString<JsonObject>(
                $$"""
                {
                  "$schema": "http://json-schema.org/draft-07/schema#",
                  "type": "object",
                  "properties": {
                    "pages": {
                      "type": "array",
                      "items": [
                        {
                          "type": "object",
                          "properties": {
                            "id": {
                              "type": "number"
                            },
                            "key": {
                              "type": "string"
                            },
                            "title": {
                              "type": "string"
                            },
                            "excerpt": {
                              "type": "string"
                            },
                            "matched_title": {
                              "anyOf": [
                                {
                                  "type": "string"
                                },
                                {
                                  "type": "null"
                                }
                              ]
                            },
                            "description": {
                              "type": "string"
                            },
                            "thumbnail": {
                              "anyOf": [
                                {
                                  "type": "null"
                                },
                                {
                                  "type": "object",
                                  "properties": {
                                    "mimetype": {
                                      "type": "string"
                                    },
                                    "width": {
                                      "type": "number"
                                    },
                                    "height": {
                                      "type": "number"
                                    },
                                    "duration": {
                                      "type": "null"
                                    },
                                    "url": {
                                      "type": "string"
                                    }
                                  },
                                  "required": [
                                    "mimetype",
                                    "url"
                                  ]
                                }
                              ]
                            }
                          },
                          "required": [
                            "id",
                            "key",
                            "title",
                            "excerpt",
                            "description"
                          ]
                        }
                      ]
                    }
                  },
                  "required": [
                    "pages"
                  ]
                }
                """.trimIndent()
            )
        )
    }
}
