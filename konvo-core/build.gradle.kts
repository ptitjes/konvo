plugins {
    id("buildsrc.convention.kotlin-multiplatform")
    alias(libs.plugins.kotlinPluginAtomicfu)
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
                api(libs.kodein)

                implementation(libs.kotlinLogging)
                implementation(libs.mcp)
                implementation(libs.bundles.ktorClient)
                implementation(libs.ktorClientCio)
                implementation(libs.uriKmp)
                implementation(libs.kim)

                implementation(project.dependencies.enforcedPlatform(libs.opentelemetry.bom))
                implementation(libs.opentelemetry.exporter.logging)
                implementation(libs.opentelemetry.exporter.otlp)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.kotlinxCoroutinesTest)
            }
        }
        all {
            languageSettings.enableLanguageFeature("ContextParameters")
        }
    }
}
