package io.github.ptitjes.konvo.plugin.core.platform

import kotlinx.io.files.*

actual val defaultFileSystem: FileSystem get() = SystemFileSystem
