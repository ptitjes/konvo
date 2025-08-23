package io.github.ptitjes.konvo.core.platform

import kotlinx.io.files.*

/**
 * Cross-platform implementation of [StoragePaths] for user-level (home) storage.
 *
 * - Linux: follows XDG Base Directory Specification, honoring XDG_*_HOME env vars.
 * - macOS: uses ~/Library/Preferences, ~/Library/Application Support, and ~/Library/Caches.
 * - Windows: uses %APPDATA% (Roaming) for config/data and %LOCALAPPDATA% for cache with sensible fallbacks.
 */
class DesktopHomeStoragePaths(private val appName: String = "konvo") : StoragePaths {

    private data class ResolvedPaths(
        val config: Path,
        val data: Path,
        val cache: Path,
    )

    private val userHome: String = System.getProperty("user.home")
    private val osName: String = System.getProperty("os.name").lowercase()

    private fun linuxPaths(): ResolvedPaths = ResolvedPaths(
        config = xdgHome("XDG_CONFIG_HOME", ".config"),
        data = xdgHome("XDG_DATA_HOME", ".local/share"),
        cache = xdgHome("XDG_CACHE_HOME", ".cache"),
    )

    private fun xdgHome(envName: String, defaultUnderHome: String): Path {
        val override = System.getenv(envName)?.takeIf { it.isNotBlank() }
        val base = override ?: "$userHome/$defaultUnderHome"
        return Path(base, appName)
    }

    private fun macPaths(): ResolvedPaths = ResolvedPaths(
        config = Path(userHome, "Library", "Preferences", appName),
        data = Path(userHome, "Library", "Application Support", appName),
        cache = Path(userHome, "Library", "Caches", appName),
    )

    private fun windowsPaths(): ResolvedPaths {
        val appData = System.getenv("APPDATA").takeUnless { it.isNullOrBlank() }
            ?: "$userHome/AppData/Roaming"

        val localAppData = System.getenv("LOCALAPPDATA").takeUnless { it.isNullOrBlank() }
            ?: "$userHome/AppData/Local"

        return ResolvedPaths(
            config = Path(appData, appName),
            data = Path(appData, appName),
            cache = Path(localAppData, appName)
        )
    }

    private val resolved: ResolvedPaths = when {
        osName.contains("win") -> windowsPaths()
        osName.contains("mac") || osName.contains("darwin") -> macPaths()
        osName.contains("linux") || osName.contains("nix") || osName.contains("nux") -> linuxPaths()
        else -> error("Unknown operating system: $osName")
    }

    override val configDirectory: Path = resolved.config
    override val dataDirectory: Path = resolved.data
    override val cacheDirectory: Path = resolved.cache
}
