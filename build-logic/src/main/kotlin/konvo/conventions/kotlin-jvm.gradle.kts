package konvo.conventions

import org.gradle.api.tasks.testing.logging.*
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_3

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")

        freeCompilerArgs.add("-Xcontext-parameters")
    }
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()

    testLogging {
        events(
            TestLogEvent.FAILED,
            TestLogEvent.PASSED,
            TestLogEvent.SKIPPED
        )
    }
}
