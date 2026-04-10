plugins {
    id("konvo.conventions.plugin")
    id("konvo.conventions.compose")
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":plugins:konvo-plugin-core"))
            implementation(project(":plugins:konvo-plugin-core-ui-compose"))
            implementation(project(":plugins:konvo-plugin-roleplay"))

            // Maybe we should either api-provide this from the core plugin?
            // Or maybe hide those behind APIs in the core plugin?
            implementation(libs.bundles.fileKit)
            implementation(libs.coil)
            implementation(libs.coilKtor)
        }
    }
}

configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.jetbrains.skiko:skiko:0.9.22.2"))
            .using(module("org.jetbrains.skiko:skiko:0.144.5"))
    }
}

compose {
    resources {
        packageOfResClass = "io.github.ptitjes.konvo.plugin.roleplay.ui.compose.resources"
    }
}
