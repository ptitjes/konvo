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
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

includeBuild("build-logic")

// Core libraries
include(":konvo-core")
include(":konvo-frontend-compose")

// MCP servers
include(":konvo-mcp-prompt-collection")
include(":konvo-mcp-web-tools")

// Frontends
include(":apps:desktop-app")

rootProject.name = "konvo"
