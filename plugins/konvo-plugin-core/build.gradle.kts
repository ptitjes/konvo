plugins {
    id("konvo.conventions.plugin")
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.bundles.kotlinxEcosystem)
            api(libs.koog)
            api(libs.syrupHost)

            implementation(libs.mcp)
            implementation(libs.bundles.ktorClient)
            implementation(libs.ktorClientCio)
            implementation(libs.uriKmp)

            implementation(project.dependencies.enforcedPlatform(libs.opentelemetry.bom))
            implementation(libs.opentelemetry.exporter.logging)
            implementation(libs.opentelemetry.exporter.otlp)
        }
    }
}
