import org.jetbrains.compose.desktop.application.dsl.*

plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlinPluginSerialization)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeGradle)
    alias(libs.plugins.composeHotReload)
}

dependencies {
    implementation(libs.slf4jSimple)
    implementation(compose.desktop.currentOs)

    implementation(project(":konvo-core"))
    implementation(project(":konvo-frontend-compose"))
}

compose.desktop {
    application {
        mainClass = "io.github.ptitjes.konvo.MainKt"

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
