package io.github.ptitjes.konvo.frontend.compose.roleplay

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.style.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.frontend.compose.toolkit.widgets.*
import io.github.ptitjes.konvo.frontend.compose.translations.*

/**
 * A selector for lorebooks.
 *
 * - Based on GenericSelector, similar to ModelSelector
 * - Accepts a nullable selection to express "no lorebook selected"
 *
 * @param label Optional label for the selector (defaults to "Lorebook")
 * @param selectedLorebook The currently selected lorebook, or null when none is selected
 * @param onLorebookSelected Callback invoked when a lorebook (or none) is selected
 * @param lorebooks List of available lorebooks
 * @param modifier Modifier for the selector component
 */
@Composable
fun LorebookSelector(
    label: String? = strings.roleplay.lorebookLabel,
    selectedLorebook: Lorebook?,
    onLorebookSelected: (Lorebook?) -> Unit,
    lorebooks: List<Lorebook>,
    modifier: Modifier = Modifier,
) {
    val none = strings.roleplay.lorebookNone
    val unnamed = strings.roleplay.lorebookUnnamed

    GenericSelector(
        modifier = modifier,
        label = label,
        selectedItem = selectedLorebook,
        onSelectItem = onLorebookSelected,
        // Include a "None" option by prepending null to the list
        options = listOf<Lorebook?>(null) + lorebooks,
        itemLabeler = { lorebookOrNull ->
            if (lorebookOrNull == null) {
                none
            } else {
                lorebookOrNull.name ?: unnamed
            }
        },
        itemOption = { lorebookOrNull ->
            if (lorebookOrNull == null) {
                Text(none)
            } else {
                Column {
                    Text(
                        text = lorebookOrNull.name ?: unnamed,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val description = lorebookOrNull.description
                    if (!description.isNullOrBlank()) {
                        Text(
                            text = description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        },
    )
}
