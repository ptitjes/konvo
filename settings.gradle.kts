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
include(":konvo-mcp-prompt-collection")
include(":konvo-mcp-web-tools")
include(":samples:discord-bot")

rootProject.name = "konvo"
