package io.github.ptitjes.konvo.plugin.core.platform

import org.kodein.di.*

actual val platformModule: DI.Module = DI.Module("Platform") {
    bindSingleton<StoragePaths> { DesktopHomeStoragePaths() }
}