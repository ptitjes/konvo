@file:OptIn(ExperimentalComposeLibrary::class)

import org.jetbrains.compose.*

plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlinPluginAtomicfu)
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeGradle)
}

kotlin {
    jvm()

    compilerOptions {
        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("androidx.compose.ui.test.ExperimentalTestApi")
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.bundles.kotlinxEcosystem)
                implementation(libs.kotlinxIoOkio)
                implementation(project(":konvo-core"))

                implementation(compose.material3)
                implementation(libs.material3Adaptive)
                implementation(compose.material3AdaptiveNavigationSuite)
                implementation(compose.materialIconsExtended)
                implementation(compose.preview)
                implementation(compose.uiTooling)

                implementation(libs.androidxLifecycleViewmodel)
                implementation(libs.kodeinCompose)

                implementation(libs.lyricist)
                implementation(libs.humanReadable)

                implementation(libs.markdownRenderer)
                implementation(libs.markdownRendererCoil)
                implementation(libs.markdownRendererCode)
                implementation(libs.fileKit)
                implementation(libs.coil)
                implementation(libs.coilKtor)
                implementation(libs.reorderable)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinxCoroutinesTest)
                implementation(compose.uiTest)
            }
        }

        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinxCoroutinesSwing)
            }
        }
    }
}
