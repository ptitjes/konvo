plugins {
    `kotlin-dsl`
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    fun DependencyHandler.plugin(dependency: Provider<PluginDependency>): Dependency =
        dependency.get().run { create("$pluginId:$pluginId.gradle.plugin:$version") }

    implementation(plugin(libs.plugins.kotlinJvm))
    implementation(plugin(libs.plugins.kotlinMultiplatform))
    implementation(plugin(libs.plugins.kotlinPluginSerialization))
    implementation(plugin(libs.plugins.ksp))
    implementation(plugin(libs.plugins.sweetSpi))
    implementation(plugin(libs.plugins.kotlinPluginCompose))
    implementation(plugin(libs.plugins.compose))
}
