package io.github.ptitjes.konvo.frontend.compose.toolkit.theme

import androidx.compose.foundation.*
import androidx.compose.runtime.*
import io.ktor.utils.io.*
import kotlinx.io.*

@Composable
internal actual fun _isInDarkTheme(): Boolean {
    // TODO Use DBus and the xdg-desktop-portal when available
    val maybeLinuxDarkTheme = remember { if (isLinuxPlatform()) detectLinuxDarkTheme() else null }
    return maybeLinuxDarkTheme ?: isSystemInDarkTheme()
}

private fun isLinuxPlatform(): Boolean =
    System.getProperty("os.name")?.contains("linux", ignoreCase = true) ?: false

/**
 * Attempts to detect dark mode on platforms that supports it.
 *
 * @return true/false when a known DE setting can be read,
 *   null if unknown/unavailable (caller should fall back to isSystemInDarkTheme()).
 */
private fun detectLinuxDarkTheme(): Boolean? {
    // Try GNOME 42+ color-scheme: 'prefer-dark' or 'default'
    val colorScheme = gnomeSettingsGetInterface("color-scheme")

    if (colorScheme != null) {
        return when (colorScheme) {
            "prefer-dark" -> true
            "default" -> false
            else -> null
        }
    }

    // Fallback: some themes encode dark in the gtk-theme name (e.g., Adwaita-dark)
    val gtkTheme = gnomeSettingsGetInterface("gtk-theme")

    if (gtkTheme != null) {
        val name = gtkTheme.lowercase()
        return "-dark" in name || name.endsWith(" dark")
    }

    // Unknown DE or no gsettings available
    return false
}

private fun gnomeSettingsGetInterface(property: String): String? {
    return try {
        val command = listOf("gsettings", "get", "org.gnome.desktop.interface", property)
        val process = ProcessBuilder(command).redirectErrorStream(true).start()

        process.inputStream.asSource().buffered().use { source ->
            source.readText().trim().removeSurrounding("'").takeUnless { it.isEmpty() }
        }.also {
            process.waitFor()
        }
    } catch (_: Exception) {
        null
    }
}
