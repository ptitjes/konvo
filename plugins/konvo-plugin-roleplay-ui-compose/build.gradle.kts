plugins {
    id("konvo.conventions.plugin")
    id("konvo.conventions.compose")
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":plugins:konvo-plugin-core-ui-compose"))
            implementation(project(":plugins:konvo-plugin-roleplay"))
        }
    }
}

compose {
    resources {
        packageOfResClass = "io.github.ptitjes.konvo.plugin.roleplay.ui.compose.resources"
    }
}
