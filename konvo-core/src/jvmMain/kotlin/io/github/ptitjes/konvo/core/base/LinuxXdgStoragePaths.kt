package io.github.ptitjes.konvo.core.base

import kotlinx.io.files.*

/**
 * Linux XDG implementation of [StoragePaths] for user-level (home) storage.
 *
 * Uses XDG_*_HOME env vars when present, otherwise falls back to standard defaults under the user's home directory.
 */
class LinuxXdgHomeStoragePaths(
    private val appName: String = "konvo",
) : StoragePaths {

    private val userHome: String = System.getProperty("user.home")

    private fun xdgHome(envName: String, defaultUnderHome: String): Path {
        val override = System.getenv(envName)?.takeIf { it.isNotBlank() }
        val base = override ?: "$userHome/$defaultUnderHome"
        return Path(base, appName)
    }

    override val configDirectory: Path = xdgHome("XDG_CONFIG_HOME", ".config")
    override val dataDirectory: Path = xdgHome("XDG_DATA_HOME", ".local/share")
    override val cacheDirectory: Path = xdgHome("XDG_CACHE_HOME", ".cache")
}

/**
 * Linux XDG implementation of [StoragePaths] for server/system deployments.
 *
 * For configuration, follows XDG_CONFIG_DIRS (first entry) or defaults to /etc/xdg.
 * For data and cache, uses standard FHS locations under /var.
 */
class LinuxXdgServerStoragePaths(
    private val appName: String = "konvo",
) : StoragePaths {
    override val configDirectory: Path = Path("/etc", appName)
    override val dataDirectory: Path = Path("/var/lib", appName)
    override val cacheDirectory: Path = Path("/var/cache", appName)
}
