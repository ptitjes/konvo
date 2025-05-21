package io.github.ptitjes.konvo.tool.web

import com.fleeksoft.ksoup.*
import com.xemantic.ai.tool.schema.meta.*
import io.github.ptitjes.konvo.tool.web.utils.*
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.serialization.*

class Fetcher(
    private val client: HttpClient,
    private val converter: HtmlToMarkdown,
) {
    @Serializable
    data class FetchRequest(
        @Description("The url of the page to fetch.")
        val url: String,
    )

    suspend fun fetch(request: FetchRequest): String {
        val response = client.get(request.url)
        val body = response.bodyAsText()

        val document = Ksoup.parse(body)

        document.select("table").remove()

        val selected = document.select("h1,h2,h3,h4,h5,h6,p,ol,ul")

        val content = selected.joinToString(separator = "\n") {
            converter.convert(it.outerHtml())
        }

        return content
    }
}
