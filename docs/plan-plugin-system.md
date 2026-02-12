# Plugins

A plugin can:

- define extension points
- contribute extensions to extension points

# Extension Points

An extension point is a Kotlin object that implements the `ExtensionPoint` interface,
with the necessary type parameters.

# Plugin API

```kotlin
// Exposed through DI
interface PluginManager {
    fun setupDiWithPlugins(): DI
}

class DefaultPluginManager : PluginManager, DIAware {
    fun setupDiWithPlugins(): DI {
        DI()
    }
}
```

```kotlin
// Exposed through DI (and composition locals?)
interface ExtensionManager {
    inline fun <reified T> getExtensions(extensionPoint: ExtensionPoint<T>): List<T>
}
```

# Plugin SPI

```kotlin
@Service
abstract class Plugin {
    // Used to force plugin to be loaded after these plugins
    // So the contributions to extension points are always topologically ordered (is that really true?)
    // Is that even necessary?
    // What happens if there is a cycle?
    val dependencies: List<Plugin>

    protected fun DI.Builder.init()

    val module by DI.Module(this::class.qualifiedName!!) { init() }
}
```

# Example Plugin definitions

```kotlin
@ServiceProvider
object PluginA : Plugin {
    val dependencies = listOf()

    override fun DI.Builder.init() {
        bindSet<SomeExtensionType>()
    }
}
```

```kotlin
@ServiceProvider
object PluginB : Plugin {
    val dependencies = listOf(PluginA)

    override fun DI.Builder.init() {
        inBindSet<SomeExtensionType> { SomeExtensionType(/* ... */) }
    }
}
```

# Example Plugin use

```kotlin
// Somewhere where the di is available
context(di: DIAware)
fun doSomething() {
    di.instance<Set<SomeExtensionType>>.forEach { extension ->
        // do something with extension
    }

    // Cool local extension properties to facade the extension point type?
    di.conversationViewStateContributions.forEach { contribution ->
        // do something with contribution
    }
}
```
