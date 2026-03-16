val projectVersion: String = libs.versions.projectVersion.get()

subprojects {
    group = "io.github.ptitjes.konvo"
    version = projectVersion
}

plugins {
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinPluginSerialization) apply false
    alias(libs.plugins.kotlinPluginCompose) apply false
}
