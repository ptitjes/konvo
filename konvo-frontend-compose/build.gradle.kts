plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlinPluginAtomicfu)
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeGradle)
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.bundles.kotlinxEcosystem)
                implementation(project(":konvo-core"))

                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.preview)

                implementation(libs.androidxLifecycleViewmodel)
                implementation(libs.kodeinCompose)

                implementation(libs.markdownRenderer)
                implementation(libs.markdownRendererCoil)
                implementation(libs.markdownRendererCode)
                implementation(libs.fileKit)
                implementation(libs.coil)
                implementation(libs.coilKtor)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
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
