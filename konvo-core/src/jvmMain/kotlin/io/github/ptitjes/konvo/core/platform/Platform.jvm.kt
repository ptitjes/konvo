package io.github.ptitjes.konvo.core.platform

import kotlinx.io.files.*

actual val defaultFileSystem: FileSystem get() = SystemFileSystem
