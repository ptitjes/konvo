plugins {
    id("konvo.conventions.library")
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.syrupHost)
            implementation(project(":libraries:konvo-lib-plugins-runtime"))
        }
    }
}
