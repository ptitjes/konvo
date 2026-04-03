package io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.adaptive

import com.slack.circuit.runtime.*

inline fun <reified T : NavScreen> Navigator.peekBackStack(): List<T> =
    peekBackStack().filterIsInstance<T>().reversed()
