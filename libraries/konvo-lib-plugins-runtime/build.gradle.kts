plugins {
    id("konvo.conventions.library")
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            api(libs.kotlinLogging)
            api(libs.syrupRuntime)
        }
    }
}
