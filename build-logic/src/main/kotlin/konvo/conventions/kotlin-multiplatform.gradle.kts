package konvo.conventions

import org.gradle.api.tasks.testing.logging.*

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm()

    jvmToolchain(21)

    sourceSets.all {
        languageSettings.enableLanguageFeature("WhenGuards")
        languageSettings.enableLanguageFeature("MultiDollarInterpolation")
        languageSettings.optIn("kotlin.time.ExperimentalTime")
        languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
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
