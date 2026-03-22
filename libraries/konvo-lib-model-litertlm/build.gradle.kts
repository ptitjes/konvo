plugins {
    id("konvo.conventions.library")
}

kotlin {
    jvm {
        // Only temporary for our test main function
        @Suppress("OPT_IN_USAGE")
        binaries {
            executable {
                mainClass = "io.github.ptitjes.konvo.lib.model.litertlm.MainKt"
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinLogging)

            implementation(libs.bundles.koogLlmClients)
            implementation(libs.koogPromptExecutorOpenAiClientBase)

            // Only temporary for our test main function
            implementation(project(":mcp:konvo-mcp-web-tools"))
            implementation(libs.kotlinxDatetime)
        }
        jvmMain.dependencies {
            api(libs.liteRtLmJvm)

            // Only temporary for our test main function
            implementation(libs.slf4jSimple)
        }
    }
}

// Only temporary for our test main function
tasks.named<JavaExec>("runJvm") {
    standardInput = System.`in`
}
