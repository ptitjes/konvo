dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenCentral()
        google()
        mavenLocal()
        maven("https://repo.kord.dev/snapshots/")
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

include(":konvo-core")
include(":konvo-frontend-compose")
include(":konvo-frontend-discord")
include(":konvo-mcp-prompt-collection")
include(":konvo-mcp-web-tools")
include(":apps:discord-bot")
include(":apps:desktop-app")

rootProject.name = "konvo"
