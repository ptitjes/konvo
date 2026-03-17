package io.github.ptitjes.konvo

import io.github.oshai.kotlinlogging.*
import io.github.ptitjes.konvo.lib.plugins.host.*
import java.awt.*

private val logger = KotlinLogging.logger { }

suspend fun main() {
    logger.info { "Starting Konvo desktop app" }
    dumpEnvironmentInformation()

    val pluginManager = bootstrapPlugins()
    val di = pluginManager.mainDi

    di.startComposeFrontend()
}

private fun dumpEnvironmentInformation() {
    logger.info {
        """
            Environment information:
            | OS:
            |   Name: ${System.getProperty("os.name")}
            |   Version: ${System.getProperty("os.version")}
            |   Architecture: ${System.getProperty("os.arch")}
            | JVM:
            |   Java version: ${System.getProperty("java.version")}
            |   Runtime name: ${System.getProperty("java.runtime.name")}
            |   Runtime version: ${System.getProperty("java.runtime.version")}
            |   VM name: ${System.getProperty("java.vm.name")}
            |   VM vendor: ${System.getProperty("java.vm.vendor")}
            |   VM version: ${System.getProperty("java.vm.version")}
            |   AWT Toolkit: ${Toolkit.getDefaultToolkit()::class.qualifiedName}
        """.trimIndent()
    }
}
