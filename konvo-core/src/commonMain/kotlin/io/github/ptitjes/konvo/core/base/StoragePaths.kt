package io.github.ptitjes.konvo.core.base

import kotlinx.io.files.Path

/**
 * Service providing standard storage directories for the application.
 *
 * Paths follow the platform conventions. On Linux, they follow the XDG Base Directory Specification.
 */
interface StoragePaths {
    /** Directory where configuration files are stored. */
    val configDirectory: Path

    /** Directory where application data files are stored. */
    val dataDirectory: Path

    /** Directory where cache files are stored. */
    val cacheDirectory: Path
}
