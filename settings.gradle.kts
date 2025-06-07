dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.kord.dev/snapshots/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

include(":konvo-core")
include(":konvo-frontend-discord")
include(":konvo-tool-web")
include(":app")

rootProject.name = "konvo"
