package konvo.conventions

import org.gradle.api.tasks.testing.logging.*

plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)

    sourceSets.all {
        languageSettings.enableLanguageFeature("WhenGuards")
        languageSettings.enableLanguageFeature("MultiDollarInterpolation")
        languageSettings.optIn("kotlin.time.ExperimentalTime")
        languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
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
