package io.github.ptitjes.konvo.frontend.compose.toolkit.widgets

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import kotlin.math.*

/**
 * A reusable outlined text field which only accepts non-negative integer input.
 * - Filters out non-digit characters as the user types.
 * - Keeps an internal text state synchronized with the provided [value].
 * - Calls [onValueChange] with the clamped integer value on each change.
 */
@Composable
fun OutlinedIntegerField(
    value: Int,
    onValueChange: (Int) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    min: Int = 0,
    max: Int? = null,
) {
    var text by remember(value) { mutableStateOf(value.toString()) }

    OutlinedTextField(
        modifier = modifier,
        value = text,
        onValueChange = { newValue ->
            val filtered = newValue.filter { it.isDigit() }
            text = filtered
            val parsed = filtered.toIntOrNull() ?: 0
            val clampedMin = max(min, parsed)
            val clamped = max?.let { m -> min(clampedMin, m) } ?: clampedMin
            onValueChange(clamped)
        },
        label = { Text(label) },
        singleLine = true,
    )
}
