plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlinPluginSerialization)
}

kotlin {
    jvm()

    sourceSets {
        commonMain {
            dependencies {
                api(libs.bundles.kotlinxEcosystem)
                api(libs.koog)
                api(libs.xemanticJsonSchema)

                implementation(libs.kotlinLogging)
                implementation(libs.mcp)
                implementation(libs.bundles.ktorClient)
                implementation(libs.ktorClientCio)
                implementation(libs.uriKmp)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}
