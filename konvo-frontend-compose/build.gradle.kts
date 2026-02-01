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

                implementation(libs.bundles.composeUi)

                implementation(libs.kodeinCompose)

                implementation(libs.lyricist)
                implementation(libs.humanReadable)

                implementation(libs.markdownRenderer)
                implementation(libs.markdownRendererCoil)
                implementation(libs.markdownRendererCode)
                implementation(libs.bundles.fileKit)
                implementation(libs.coil)
                implementation(libs.coilKtor)
                implementation(libs.reorderable)
                implementation(libs.placeholder)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinxCoroutinesTest)
                implementation(libs.composeUiTest)
            }
        }

        jvmMain {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinxCoroutinesSwing)
            }
        }
        all {
            languageSettings.enableLanguageFeature("NestedTypeAliases")
            languageSettings.enableLanguageFeature("ContextParameters")
        }
    }
}

compose {
    resources {
        packageOfResClass = "io.github.ptitjes.konvo.frontend.compose.resources"
    }
}
