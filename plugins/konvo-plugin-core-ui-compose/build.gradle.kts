plugins {
    id("konvo.conventions.plugin")
    id("konvo.conventions.compose")
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":plugins:konvo-plugin-core"))

            implementation(libs.bundles.kotlinxEcosystem)
            implementation(libs.kotlinxIoOkio)

            api(libs.kodeinCompose)

            api(libs.circuit)

            implementation(libs.humanReadable)

            implementation(libs.markdownRenderer)
            implementation(libs.markdownRendererCoil)
            implementation(libs.markdownRendererCode)
            implementation(libs.bundles.fileKit)
            implementation(libs.coil)
            implementation(libs.coilKtor)
            implementation(libs.reorderable)
            implementation(libs.placeholder)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinxCoroutinesSwing)
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
        packageOfResClass = "io.github.ptitjes.konvo.plugin.core.ui.compose.resources"
    }
}
