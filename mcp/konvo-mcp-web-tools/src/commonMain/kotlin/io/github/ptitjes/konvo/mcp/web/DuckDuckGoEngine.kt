package io.github.ptitjes.konvo.mcp.web

import com.fleeksoft.ksoup.*
import io.github.ptitjes.konvo.mcp.web.utils.*
import io.ktor.client.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.schema.generator.json.*
import kotlinx.serialization.*

class DuckDuckGoEngine(
    private val client: HttpClient,
    private val converter: HtmlToMarkdown,
) {
    @Serializable
    data class SearchRequest(
        @SerialDescription("The query to search for. It should only contain the search term and be relatively short.")
        val query: String,
        @SerialDescription("The maximum number of search results to query.")
        val maxResults: Int? = null,
        @SerialDescription("The region to search in.")
        val region: Region = Region.AllRegions,
        @SerialDescription("The time frame to search in.")
        val timeFrame: TimeFrame = TimeFrame.AnyTime,
    )

    @Suppress("EnumEntryName", "unused")
    @Serializable
    enum class Region(val value: String) {
        AllRegions(""),
        Argentina("ar-es"),
        Australia("au-en"),
        Austria("at-de"),
        Belgium_fr("be-fr"),
        Belgium_nl("be-nl"),
        Brazil("br-pt"),
        Bulgaria("bg-bg"),
        Canada_en("ca-en"),
        Canada_fr("ca-fr"),
        Catalonia("ct-ca"),
        Chile("cl-es"),
        China("cn-zh"),
        Colombia("co-es"),
        Croatia("hr-hr"),
        CzechRepublic("cz-cs"),
        Denmark("dk-da"),
        Estonia("ee-et"),
        Finland("fi-fi"),
        France("fr-fr"),
        Germany("de-de"),
        Greece("gr-el"),
        HongKong("hk-tzh"),
        Hungary("hu-hu"),
        Iceland("is-is"),
        India_en("in-en"),
        Indonesia_en("id-en"),
        Ireland("ie-en"),
        Israel_en("il-en"),
        Italy("it-it"),
        Japan("jp-jp"),
        Korea("kr-kr"),
        Latvia("lv-lv"),
        Lithuania("lt-lt"),
        Malaysia_en("my-en"),
        Mexico("mx-es"),
        Netherlands("nl-nl"),
        NewZealand("nz-en"),
        Norway("no-no"),
        Pakistan_en("pk-en"),
        Peru("pe-es"),
        Philippines_en("ph-en"),
        Poland("pl-pl"),
        Portugal("pt-pt"),
        Romania("ro-ro"),
        Russia("ru-ru"),
        SaudiArabia("xa-ar"),
        Singapore("sg-en"),
        Slovakia("sk-sk"),
        Slovenia("sl-sl"),
        SouthAfrica("za-en"),
        Spain_ca("es-ca"),
        Spain_es("es-es"),
        Sweden("se-sv"),
        Switzerland_de("ch-de"),
        Switzerland_fr("ch-fr"),
        Taiwan("tw-tzh"),
        Thailand_en("th-en"),
        Turkey("tr-tr"),
        US_English("us-en"),
        US_Spanish("us-es"),
        Ukraine("ua-uk"),
        UnitedKingdom("uk-en"),
        Vietnam_en("vn-en"),
    }

    @Suppress("unused")
    @Serializable
    @SerialDescription("The time frame to search in.")
    enum class TimeFrame(val value: String) {
        @SerialDescription("Any time frame.")
        @SerialName("any-time")
        AnyTime(""),

        @SerialDescription("During the past day.")
        @SerialName("past-day")
        PastDay("d"),

        @SerialDescription("During the past week.")
        @SerialName("past-week")
        PastWeek("w"),

        @SerialDescription("During the past month.")
        @SerialName("past-month")
        PastMonth("m"),

        @SerialDescription("During the past year.")
        @SerialName("past-year")
        PastYear("y"),
    }

    @Serializable
    data class SearchResponse(
        @SerialDescription("The list of search results.")
        val results: List<SearchResult>,
    )

    @Serializable
    data class SearchResult(
        @SerialDescription("The title of the search result.")
        val title: String,
        @SerialDescription("The URL of the search result.")
        val url: String,
        @SerialDescription("The snippet of the search result.")
        val snippet: String,
    )

    suspend fun search(request: SearchRequest): SearchResponse {
        val response = client.submitForm(
            url = "https://lite.duckduckgo.com/lite/",
            formParameters = parameters {
                append("q", value = request.query)
                append("kl", request.region.value)
                append("df", request.timeFrame.value)
                append("o", "json")
            }
        ) {
            headers {
                append(HttpHeaders.Referrer, "https://lite.duckduckgo.com/")
                append("Sec-Fetch-User", "?1")
            }
        }

        val body = response.bodyAsText()
        val document = Ksoup.parse(body)
        val lastTable = document.select("table").last()!!
        val resultRows = lastTable.select("tr").chunked(4)
        val filteredRows = request.maxResults?.let { resultRows.take(it) } ?: resultRows

        return SearchResponse(
            results = filteredRows.mapNotNull { rows ->
                if (rows.size != 4) return@mapNotNull null

                val link = rows[0].select("a")
                val title = link.text()
                val url = link.attr("href")
                val snippet = converter.convert(rows[1].select("td.result-snippet").html()).trim()
                SearchResult(title, url, snippet)
            },
        )
    }
}
