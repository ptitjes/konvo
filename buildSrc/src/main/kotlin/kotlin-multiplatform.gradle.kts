package buildsrc.convention

import org.gradle.api.tasks.testing.logging.*

plugins {
    kotlin("multiplatform")
}

kotlin {
    jvm {
    }

    jvmToolchain(17)
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
