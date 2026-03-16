package konvo.conventions

plugins {
    id("konvo.conventions.kotlin-multiplatform")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val versionCatalog = versionCatalogs.named("libs")

kotlin {
    sourceSets.all {
        languageSettings.enableLanguageFeature("WhenGuards")
        languageSettings.enableLanguageFeature("MultiDollarInterpolation")
        languageSettings.optIn("kotlin.time.ExperimentalTime")
        languageSettings.optIn("kotlin.uuid.ExperimentalUuidApi")
    }

    sourceSets {
        commonMain.dependencies {
            implementation(versionCatalog.findLibrary("kotlinxCoroutines").get())
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation(versionCatalog.findLibrary("kotlinxCoroutinesTest").get())
        }
    }
}
