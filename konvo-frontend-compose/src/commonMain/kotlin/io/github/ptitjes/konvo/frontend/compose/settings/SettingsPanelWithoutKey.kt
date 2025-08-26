package io.github.ptitjes.konvo.frontend.compose.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*

@Composable
fun SettingsPanelWithoutKey(
    section: SettingsSection.WithoutKey,
) {
    val baseModifier = if (section.scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier
    Column(modifier = baseModifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)) {
        section.panel()
    }
}
