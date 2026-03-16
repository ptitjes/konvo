package konvo.conventions

import dev.whyoleg.sweetspi.gradle.*

plugins {
    id("konvo.conventions.library")
    id("com.google.devtools.ksp")
    id("dev.whyoleg.sweetspi")
}

kotlin {
    withSweetSpi()
}
