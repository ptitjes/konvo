package io.github.ptitjes.konvo.frontend.discord.toolkit

import ai.koog.prompt.markdown.*

fun MarkdownContentBuilder.blockquote(block: MarkdownContentBuilder.() -> Unit) {
    blockquote(MarkdownContentBuilder().apply(block).build().trim())
}

fun MarkdownContentBuilder.subscript(text: String) {
    text.split("\n").forEach {
        +(if (it.isBlank()) "" else "-# $it")
    }
}

fun MarkdownContentBuilder.subscript(block: MarkdownContentBuilder.LineContext.() -> Unit) {
    subscript(MarkdownContentBuilder().apply { line { block() } }.build())
}
