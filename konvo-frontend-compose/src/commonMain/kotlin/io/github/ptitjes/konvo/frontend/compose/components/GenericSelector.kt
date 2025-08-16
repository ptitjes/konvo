package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GenericSelector(
    label: String? = null,
    selectedItem: T,
    onSelectItem: (T) -> Unit,
    options: List<T>,
    itemLabeler: (T) -> String,
    itemOption: @Composable (T) -> Unit = { option -> DefaultItemOption(itemLabeler, option) },
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            label = {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            },
            value = itemLabeler(selectedItem),
            onValueChange = {},
            readOnly = true,
            maxLines = 1,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { itemOption(option) },
                    onClick = {
                        onSelectItem(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun <T> DefaultItemOption(itemLabeler: (T) -> String, option: T) {
    Text(
        text = itemLabeler(option),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
