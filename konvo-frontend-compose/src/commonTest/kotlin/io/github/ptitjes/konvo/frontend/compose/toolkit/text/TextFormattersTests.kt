package io.github.ptitjes.konvo.frontend.compose.toolkit.text

import kotlin.test.*

class TextFormattersTests {

    @Test
    fun `truncatePreview collapses whitespace and adds ellipsis when needed`() {
        val text =
            "This   is a\nlong   preview   that should be truncated at some point because it exceeds the maximum length allowed by the UI."
        val result = TextFormatters.truncatePreview(text, maxChars = 50)
        // Should not exceed 51 including ellipsis
        assert(result.length <= 51)
        // Should end with ellipsis
        assert(result.endsWith("…"))
        // Should have single spaces
        assert(!result.contains("  "))
    }
}
