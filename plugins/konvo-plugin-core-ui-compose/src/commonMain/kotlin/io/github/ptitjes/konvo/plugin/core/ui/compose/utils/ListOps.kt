package io.github.ptitjes.konvo.plugin.core.ui.compose.utils

fun <T> List<T>.mutate(block: MutableList<T>.() -> Unit): List<T> =
    toMutableList().apply(block).toList()
