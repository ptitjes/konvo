@file:OptIn(kotlin.time.ExperimentalTime::class)

package io.github.ptitjes.konvo.core.util

import kotlin.time.*

/**
 * Abstraction for retrieving current time. Useful for testing.
 */
interface TimeProvider {
    fun now(): Instant
}

object SystemTimeProvider : TimeProvider {
    private val clock = Clock.System

    override fun now(): Instant = clock.now()
}
