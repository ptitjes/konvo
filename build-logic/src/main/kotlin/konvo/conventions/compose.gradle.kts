package konvo.conventions

plugins {
    id("konvo.conventions.kotlin-multiplatform")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.compose")
}

val versionCatalog = versionCatalogs.named("libs")

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(versionCatalog.findBundle("composeUi").get())
        }

        commonTest.dependencies {
            implementation(versionCatalog.findLibrary("composeUiTest").get())
        }
    }
}
