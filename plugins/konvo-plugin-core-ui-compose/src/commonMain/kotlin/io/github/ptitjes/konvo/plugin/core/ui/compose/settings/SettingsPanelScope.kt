package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import androidx.compose.material3.*

interface SettingsPanelScope {
    suspend fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        withDismissAction: Boolean = false,
        duration: SnackbarDuration =
            if (actionLabel == null) SnackbarDuration.Short else SnackbarDuration.Indefinite
    ): SnackbarResult
}
