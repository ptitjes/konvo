package io.github.ptitjes.konvo.core.conversations.model

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

    @Test
    fun `truncatePreview correctly handles markdown images`() {
        val text =
            """
                ![image](https://example.com/image.png)

                This is a long preview that should be truncated at some point because it exceeds the maximum length allowed by the UI.
            """.trimIndent()
        val result = TextFormatters.truncatePreview(text, maxChars = 50)
        // Should not exceed 51 including ellipsis
        assert(result.length <= 51)
        // Should end with ellipsis
        assert(result.endsWith("…"))
        // Should have single spaces
        assert(!result.contains("  "))
    }
}
