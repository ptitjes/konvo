package io.github.ptitjes.konvo.core.util

import kotlin.uuid.*

/**
 * Abstraction for generating unique identifiers.
 */
interface IdGenerator {
    fun newId(): String
}

@OptIn(ExperimentalUuidApi::class)
object UuidIdGenerator : IdGenerator {
    override fun newId(): String = Uuid.random().toString()
}
