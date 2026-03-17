package konvo.conventions

import dev.whyoleg.sweetspi.gradle.*

plugins {
    id("konvo.conventions.library")
    id("com.google.devtools.ksp")
    id("dev.whyoleg.sweetspi")
}

val versionCatalog = versionCatalogs.named("libs")

kotlin {
    withSweetSpi()

    sourceSets {
        commonMain.dependencies {
            implementation(versionCatalog.findLibrary("syrupRuntime").get())
            implementation(project(":libraries:konvo-lib-plugins-runtime"))
        }
    }
}
