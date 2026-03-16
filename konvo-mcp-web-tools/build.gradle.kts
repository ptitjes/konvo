@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import com.github.jengelman.gradle.plugins.shadow.tasks.*
import org.jetbrains.kotlin.gradle.*

plugins {
    id("konvo.conventions.library")
    alias(libs.plugins.shadow)
}

kotlin {
    jvm {
        binaries {
            executable {
                mainClass = "io.github.ptitjes.konvo.mcp.web.MainKt"
            }
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.bundles.kotlinxEcosystem)
                implementation(libs.kotlinxCli)
                implementation(libs.mcp)
                implementation(libs.ktorServerCio)
                implementation(libs.xemanticJsonSchema)

                implementation(libs.bundles.ktorClient)
                implementation(libs.ktorClientCio)

                implementation(libs.slf4jSimple)
                implementation(libs.ksoup)
                implementation(libs.flexmarkHtmlMarkdownConverter)
            }
        }

        commonTest {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

tasks.named<ShadowJar>("shadowJar") {
    manifest {
        attributes["Main-Class"] = "io.github.ptitjes.konvo.mcp.web.MainKt"
    }
}

tasks.withType<Sync> {
    destinationDir = File("/opt/konvo/mcp-servers/konvo-web-tools")
}
