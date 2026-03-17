package io.github.ptitjes.konvo.lib.plugins.host

import io.github.ptitjes.syrup.*
import io.github.ptitjes.syrup.host.*
import kotlinx.coroutines.*
import org.kodein.di.*
import kotlin.coroutines.*

suspend fun bootstrapPlugins(): PluginManager {
    val coroutineContext = currentCoroutineContext()
    val rootScope = CoroutineScope(coroutineContext + Dispatchers.Default)

    fun coroutineScopeFor(plugin: Plugin): CoroutineScope = CoroutineScope(
        rootScope.coroutineContext + Dispatchers.Default + CoroutineName("plugin:${plugin.id}"),
    )

    val pluginManager = assemblePlugins(SyrupLogger) {
        loadPlugins()

        contributePluginBindings { plugin ->
            bind<CoroutineContext> { singleton { coroutineScopeFor(plugin).coroutineContext } }
        }
    }

    return pluginManager
}
