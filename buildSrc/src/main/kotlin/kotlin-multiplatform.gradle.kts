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
