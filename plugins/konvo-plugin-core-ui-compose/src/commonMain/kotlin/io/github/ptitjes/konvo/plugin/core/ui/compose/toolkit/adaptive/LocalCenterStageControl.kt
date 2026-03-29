package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import androidx.compose.runtime.*

val LocalCenterStageControl = compositionLocalOf<CenterStageControl> {
    error("No CenterStageControl provided")
}
