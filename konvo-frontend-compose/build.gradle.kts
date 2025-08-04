plugins {
    id("buildsrc.convention.kotlin-multiplatform")
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

                implementation(libs.markdownRenderer)
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
