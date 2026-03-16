package io.github.ptitjes.konvo.plugin.core.util

import kotlin.uuid.*

/**
 * Abstraction for generating unique identifiers.
 */
interface IdGenerator {
    fun newId(): String
}

object UuidIdGenerator : IdGenerator {
    override fun newId(): String = Uuid.random().toString()
}
