plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    application
}

dependencies {
    implementation(libs.slf4jSimple)

    implementation(project(":konvo-core"))
    implementation(project(":konvo-backend-mcp"))
    implementation(project(":konvo-frontend-discord"))
}

application {
    applicationName = "konvo"
    mainClass = "io.github.ptitjes.konvo.MainKt"
}

distributions {
    main {
        distributionBaseName = "konvo"
    }
}

tasks.withType<Sync> {
    destinationDir = File("/opt/konvo")
    preserve {
        include("data/**")
        include("config/**")
        include("mcp-servers/**")
    }
}
