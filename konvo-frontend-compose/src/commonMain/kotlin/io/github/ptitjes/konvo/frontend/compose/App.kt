package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.material.icons.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.*
import androidx.compose.material3.adaptive.navigationsuite.*
import androidx.compose.runtime.*
import androidx.window.core.layout.*
import io.github.ptitjes.konvo.frontend.compose.components.ConversationsListDetailPane
import io.github.ptitjes.konvo.frontend.compose.theme.*

@Composable
fun App() {
    val adaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfo()

    KonvoTheme {
        NavigationSuiteScaffold(
            layoutType = suiteTypeFromAdaptiveInfo(adaptiveInfo),
            navigationSuiteItems = {
                item(
                    selected = true,
                    onClick = {},
                    icon = { Icon(imageVector = Icons.AutoMirrored.Default.Chat, contentDescription = null) },
                    badge = {},
                )
            },
        ) {
            ConversationsListDetailPane(adaptiveInfo)
        }
    }
}

private fun suiteTypeFromAdaptiveInfo(adaptiveInfo: WindowAdaptiveInfo): NavigationSuiteType {
    return with(adaptiveInfo) {
        if (windowPosture.isTabletop || windowSizeClass.windowHeightSizeClass == WindowHeightSizeClass.COMPACT) {
            NavigationSuiteType.NavigationBar
        } else if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED ||
            windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.MEDIUM
        ) {
            NavigationSuiteType.NavigationRail
        } else {
            NavigationSuiteType.NavigationBar
        }
    }
}
