package io.github.ptitjes.konvo.tool.web

import com.fleeksoft.ksoup.*
import io.github.ptitjes.konvo.tool.web.utils.HtmlToMarkdown
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*

class Fetcher(
    private val client: HttpClient,
    private val converter: HtmlToMarkdown,
) {
    suspend fun fetch(url: String): String {
        val response = client.get(url)
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
