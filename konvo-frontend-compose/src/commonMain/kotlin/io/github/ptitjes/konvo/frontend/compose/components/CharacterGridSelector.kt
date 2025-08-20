package io.github.ptitjes.konvo.frontend.compose.components

import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.*
import androidx.compose.ui.unit.*
import coil3.compose.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.frontend.compose.util.*

/**
 * An alternative pretty selector for characters showing avatars in a responsive grid.
 * Each grid item displays the character avatar with its name as an overlay.
 *
 * This component does not render a label like the dropdown selector; wrap it with your own
 * label if needed in the form.
 *
 * @param selectedCharacter The currently selected character
 * @param onCharacterSelected Callback for when a character is selected
 * @param characters List of available characters
 * @param modifier The modifier to apply to the grid
 * @param minCellSize Minimum cell size used for the adaptive grid
 * @param cellSpacing Spacing between cells
 */
@Composable
fun CharacterGridSelector(
    selectedCharacter: CharacterCard,
    onCharacterSelected: (CharacterCard) -> Unit,
    characters: List<CharacterCard>,
    modifier: Modifier = Modifier,
    minCellSize: Dp = 120.dp,
    cellSpacing: Dp = 12.dp,
) {
    val filteredTags by rememberSetting(CharacterSettingsKey, null) { characterSettings ->
        characterSettings.filteredTags.map { it.lowercase() }.toSet()
    }

    when (val filteredTags = filteredTags) {
        null -> FullSizeProgressIndicator()
        else -> {
            val filteredCharacters = characters.filter { character ->
                !character.tags.any { it.lowercase() in filteredTags }
            }

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = minCellSize),
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(cellSpacing),
                horizontalArrangement = Arrangement.spacedBy(cellSpacing),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                items(filteredCharacters, key = { it.id }) { character ->
                    val isSelected = character.id == selectedCharacter.id
                    CharacterGridItem(
                        character = character,
                        selected = isSelected,
                        onClick = { onCharacterSelected(character) },
                    )
                }
            }
        }
    }
}

@Composable
private fun CharacterGridItem(
    character: CharacterCard,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = CardDefaults.shape
    val border: BorderStroke? = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null

    Card(
        modifier = modifier
            .aspectRatio(1f)
            .clip(shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = border,
        elevation = if (selected) CardDefaults.cardElevation(defaultElevation = 6.dp) else CardDefaults.cardElevation(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (character.avatarUrl != null) {
                AsyncImage(
                    model = character.avatarUrl,
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                // Fallback colored background with initial
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = character.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (character.characterBook != null) {
                // Small book icon at the top-right when the character has a character book
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .background(color = Color(0x66000000), shape = CircleShape)
                        .padding(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Book,
                        contentDescription = "Has character book",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp),
                    )
                }
            }

            // Name overlay with scrim
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xAA000000))
                        )
                    )
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Text(
                    text = character.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White,
                    textAlign = TextAlign.Start,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
