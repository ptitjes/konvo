package konvo.conventions

import org.gradle.api.tasks.testing.logging.*

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvmToolchain(21)

    sourceSets.all {
        languageSettings.enableLanguageFeature("WhenGuards")
        languageSettings.enableLanguageFeature("MultiDollarInterpolation")
        languageSettings.enableLanguageFeature("NestedTypeAliases")
        languageSettings.enableLanguageFeature("ContextParameters")
        languageSettings.optIn("kotlin.time.ExperimentalTime")
        languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
        languageSettings.optIn("androidx.compose.ui.test.ExperimentalTestApi")
    }

    sourceSets {
        commonMain.dependencies {
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
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
