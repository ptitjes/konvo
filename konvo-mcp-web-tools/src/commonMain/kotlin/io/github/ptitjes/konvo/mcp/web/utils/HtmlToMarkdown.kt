package io.github.ptitjes.konvo.mcp.web.utils

import com.vladsch.flexmark.html2md.converter.FlexmarkHtmlConverter
import com.vladsch.flexmark.util.data.DataHolder
import com.vladsch.flexmark.util.data.MutableDataSet

class HtmlToMarkdown {
    private val options: DataHolder = MutableDataSet().apply {
        set(FlexmarkHtmlConverter.SETEXT_HEADINGS, false)
    }

    private val converter = FlexmarkHtmlConverter.builder(options).build()

    fun convert(html: String): String = converter.convert(html)
}
