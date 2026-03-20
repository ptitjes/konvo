package konvo.conventions

import dev.whyoleg.sweetspi.gradle.*
import konvo.tasks.*

plugins {
    id("konvo.conventions.library")
    id("com.google.devtools.ksp")
    id("dev.whyoleg.sweetspi")
}

private val generatePluginConfig by tasks.registering(PluginConfigGeneratorTask::class) {
    group = project.group.toString()
    name = buildString {
        append("konvo-")
        append(project.name.removePrefix("konvo-plugin-"))
    }
    packageName = buildString {
        append(project.group)
        append(".")
        append(project.name.removePrefix("konvo-").replace('-', '.'))
    }
    version = project.version.toString()

    destinationDir.set(layout.buildDirectory.dir("generated/konvo-plugin-id/kotlin"))
}

kotlin {
    withSweetSpi()

    sourceSets {
        commonMain { kotlin.srcDir(generatePluginConfig.map { it.destinationDir }) }

        val versionCatalog = versionCatalogs.named("libs")

        commonMain.dependencies {
            implementation(versionCatalog.findLibrary("syrupRuntime").get())
            implementation(project(":libraries:konvo-lib-plugins-runtime"))
        }
    }
}