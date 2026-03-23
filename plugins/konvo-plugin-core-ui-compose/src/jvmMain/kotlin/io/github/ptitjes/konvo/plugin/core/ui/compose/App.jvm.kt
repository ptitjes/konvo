package io.github.ptitjes.konvo.plugin.core.ui.compose

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import io.github.kdroidfilter.nucleus.window.*
import io.github.kdroidfilter.nucleus.window.styling.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.images.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.theme.*
import org.kodein.di.*
import org.kodein.di.compose.*

actual val appModule: DI.Module = DI.Module("app") {
    bind<App> { singleton { DesktopApp(di) } }
}

class DesktopApp(private val di: DI) : App {
    @Composable
    override operator fun invoke(onCloseRequest: () -> Unit) {
        withDI(di) {
            CoilImageLoader()

            KonvoTheme {
                val containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                val contentColor = MaterialTheme.colorScheme.onSurface

                NucleusDecoratedWindowTheme(
                    isDark = isSystemInDarkTheme(),
                    windowStyle = DecoratedWindowStyle(
                        colors = DecoratedWindowColors(
                            border = containerColor.copy(alpha = 0.7f),
                            borderInactive = containerColor.copy(alpha = 0.7f),
                        ),
                        metrics = DecoratedWindowMetrics()
                    ),
                    titleBarStyle = TitleBarStyle(
                        colors = TitleBarColors(
                            background = containerColor,
                            inactiveBackground = containerColor,
                            content = contentColor,
                            border = containerColor,
                        ),
                        metrics = TitleBarMetrics(height = 48.dp),
                        icons = TitleBarIcons(),
                    )
                ) {
                    ProvideI18nStrings {
                        DecoratedWindow(
                            onCloseRequest = onCloseRequest,
                            title = "Konvo",
//                            state = rememberWindowState(width = 375.dp, height = 667.dp),
                            state = rememberWindowState(width = 1280.dp, height = 720.dp),
                            icon = appIconPainter(),
                            onKeyEvent = { event: KeyEvent ->
                                if (event.type == KeyEventType.KeyUp && event.isCtrlPressed && event.key == Key.Q) {
                                    onCloseRequest()
                                    true
                                } else {
                                    false
                                }
                            },
                        ) {
                            TitleBar {
                                Box(modifier = Modifier.align(Alignment.Start)) {
                                    icon?.let { icon ->
                                        Icon(
                                            modifier = Modifier.size(40.dp).padding(8.dp),
                                            painter = icon,
                                            contentDescription = "Konvo",
                                            tint = Color.Unspecified,
                                        )
                                    }
                                }
                            }

                            MainScreen()
                        }
                    }
                }
            }
        }
    }
}