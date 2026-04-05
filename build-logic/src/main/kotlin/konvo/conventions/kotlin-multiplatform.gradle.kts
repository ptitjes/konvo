package konvo.conventions

import org.gradle.api.tasks.testing.logging.*
import org.jetbrains.kotlin.gradle.dsl.*

plugins {
    kotlin("multiplatform")
    id("de.infix.testBalloon")
}

kotlin {
    jvmToolchain(21)

    compilerOptions {
        languageVersion = KotlinVersion.KOTLIN_2_3

        optIn.add("kotlin.time.ExperimentalTime")
        optIn.add("kotlin.uuid.ExperimentalUuidApi")

        freeCompilerArgs.add("-Xcontext-parameters")
    }

    val versionCatalog = versionCatalogs.named("libs")

    sourceSets {
        commonMain.dependencies {
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(versionCatalog.findLibrary("testBalloonFrameworkCore").get())
        }
    }
}

tasks.withType<AbstractTestTask>().configureEach {
    reports {
        html.required.set(true)
        junitXml.required.set(true)
    }
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
