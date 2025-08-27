package io.github.ptitjes.konvo.frontend.compose.utils

fun <T> List<T>.mutate(block: MutableList<T>.() -> Unit): List<T> =
    toMutableList().apply(block).toList()
