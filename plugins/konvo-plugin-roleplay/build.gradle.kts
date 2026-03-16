plugins {
    id("konvo.conventions.plugin")
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":plugins:konvo-plugin-core"))
        }
    }
}
