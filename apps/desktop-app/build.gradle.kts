import org.jetbrains.compose.desktop.application.dsl.*

plugins {
    id("konvo.conventions.library")
    id("konvo.conventions.compose")
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.slf4jSimple)
            implementation(libs.kotlinLogging)
            implementation(libs.syrupHost)

            implementation(project(":plugins:konvo-plugin-core"))
            implementation(project(":plugins:konvo-plugin-core-ui-compose"))
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }
}

compose.desktop {
    application {
        mainClass = "io.github.ptitjes.konvo.MainKt"

        // https://youtrack.jetbrains.com/issue/SKIKO-890/Crash-when-using-WLToolkit
        // jvmArgs += listOf("-Dawt.toolkit.name=auto")

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Rpm)
            packageName = "Konvo"
            packageVersion = libs.versions.projectVersion.get()

            val iconsRoot = project.file("desktop-icons")
            macOS {
                iconFile.set(iconsRoot.resolve("icon-mac.icns"))
            }
            windows {
                iconFile.set(iconsRoot.resolve("icon-windows.ico"))
                upgradeUuid = "b248cbfe-b7b5-4171-8225-4d6322f353e1".uppercase()
            }
            linux {
                iconFile.set(iconsRoot.resolve("icon-linux.png"))
            }
        }

        buildTypes.release.proguard {
            configurationFiles.from(project.file("rules.pro"))
        }
    }
}
