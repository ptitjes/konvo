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

// Plugins
include(":plugins:konvo-plugin-core")
include(":plugins:konvo-plugin-core-ui-compose")

// MCP servers
include(":mcp:konvo-mcp-prompt-collection")
include(":mcp:konvo-mcp-web-tools")

// Frontends
include(":apps:desktop-app")

rootProject.name = "konvo"
