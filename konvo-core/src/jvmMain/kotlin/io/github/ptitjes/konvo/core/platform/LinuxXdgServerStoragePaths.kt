package io.github.ptitjes.konvo.core.platform

import kotlinx.io.files.*

/**
 * Linux XDG implementation of [StoragePaths] for server/system deployments.
 *
 * For configuration, follows XDG_CONFIG_DIRS (first entry) or defaults to /etc/xdg.
 * For data and cache, uses standard FHS locations under /var.
 */
class LinuxXdgServerStoragePaths(appName: String = "konvo") : StoragePaths {
    override val configDirectory: Path = Path("/etc", appName)
    override val dataDirectory: Path = Path("/var/lib", appName)
    override val cacheDirectory: Path = Path("/var/cache", appName)
}
