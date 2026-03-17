package io.github.ptitjes.konvo.lib.plugins.host

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.syrup.host.*

private val logger = KotlinLogging.logger { }

internal object SyrupLogger : Logger {
    override fun trace(messageBuilder: () -> String) = logger.trace(messageBuilder)
    override fun debug(messageBuilder: () -> String) = logger.debug(messageBuilder)
    override fun info(messageBuilder: () -> String) = logger.info(messageBuilder)
    override fun warn(messageBuilder: () -> String) = logger.warn(messageBuilder)
    override fun error(messageBuilder: () -> String) = logger.error(messageBuilder)
    override fun error(throwable: Throwable, messageBuilder: () -> String) = logger.error(throwable, messageBuilder)
}
