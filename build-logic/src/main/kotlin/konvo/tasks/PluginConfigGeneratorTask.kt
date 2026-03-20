package konvo.tasks

import org.gradle.api.*
import org.gradle.api.file.*
import org.gradle.api.internal.file.*
import org.gradle.api.provider.*
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.*
import javax.inject.*

internal abstract class PluginConfigGeneratorTask : DefaultTask() {

    @get:Input
    val group: Property<String> = project.objects.property<String>()

    @get:Input
    val name: Property<String> = project.objects.property<String>()

    @get:Input
    val version: Property<String> = project.objects.property<String>()

    @get:Input
    val packageName: Property<String> = project.objects.property<String>()

    @get:OutputDirectory
    val destinationDir: DirectoryProperty = project.objects.directoryProperty()

    @get:Inject
    abstract val fs: FileSystemOperations

    @get:Inject
    abstract val files: FileOperations

    @get:Inject
    abstract val providers: ProviderFactory

    @TaskAction
    fun execute() {
        val pluginConfigFileContents = providers.provider {
            files.resources.text.fromString(
                """
                  |@file:Suppress("ConstPropertyName")
                  |
                  |package ${packageName.get()}
                  |
                  |import io.github.ptitjes.syrup.PluginId
                  |
                  |internal object PluginConfig {
                  |    const val Group = "${group.get()}"
                  |    const val Name = "${name.get()}"
                  |    const val Version = "${version.get()}"
                  |
                  |    val Id = PluginId("${group.get()}:${name.get()}")
                  |}
                """.trimMargin()
            )
        }

        fs.sync {
            from(pluginConfigFileContents) {
                rename { "PluginConfig.kt" }
                into(packageName.map { it.replace('.', '/') })
            }

            into(destinationDir)
        }
    }
}
