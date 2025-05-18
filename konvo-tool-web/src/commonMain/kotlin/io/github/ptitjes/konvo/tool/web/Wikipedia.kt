package io.github.ptitjes.konvo.tool.web

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.json.*

class Wikipedia(
    private val client: HttpClient,
    private val baseUrl: String = "https://en.wikipedia.org/w/",
    private val pathPrefix: String = "rest.php/v1/",
) {

    suspend fun search(query: String, limit: Int? = DEFAULT_LIMIT): JsonObject {
        return request("search/page") {
            append("q", query)
            append("limit", (limit ?: DEFAULT_LIMIT).toString())
        }
    }

    suspend fun getPage(key: String): JsonObject {
        return request("page/$key")
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
