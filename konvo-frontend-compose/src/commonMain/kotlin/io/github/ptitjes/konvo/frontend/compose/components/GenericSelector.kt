package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> GenericSelector(
    label: String,
    selectedItem: T,
    onSelectItem: (T) -> Unit,
    options: List<T>,
    itemLabeler: (T) -> String,
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
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleSmall
                )
            },
            value = itemLabeler(selectedItem),
            onValueChange = {},
            readOnly = true,
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
                    text = { Text(itemLabeler(option)) },
                    onClick = {
                        onSelectItem(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
