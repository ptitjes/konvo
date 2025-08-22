plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeGradle)
    alias(libs.plugins.composeHotReload)
}

dependencies {
    implementation(libs.slf4jSimple)
    implementation(compose.desktop.currentOs)

    implementation(project(":konvo-core"))
    implementation(project(":konvo-frontend-compose"))
}

compose {
    desktop {
        application {
            mainClass = "io.github.ptitjes.konvo.MainKt"
        }
    }
}
