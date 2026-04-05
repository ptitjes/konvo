package io.github.ptitjes.konvo.plugin.roleplay.formats

import de.stefan_oltmann.kim.format.png.*
import de.stefan_oltmann.kim.format.png.chunk.*
import de.stefan_oltmann.kim.input.*
import io.github.ptitjes.konvo.plugin.roleplay.*
import kotlinx.io.bytestring.*
import kotlin.io.encoding.*

internal fun ByteString.extractCharacterCard(id: String): CharacterCard {
    val chunks = extractTextChunks()
    val ccv3Chunk = chunks.firstOrNull { it.getKeyword() == "ccv3" }
    if (ccv3Chunk != null) return ccv3Chunk.extractChunkText().parseCharacterCard(id)

    val charaChunk = chunks.firstOrNull { it.getKeyword() == "chara" }
    if (charaChunk != null) return charaChunk.extractChunkText().parseCharacterCard(id)

    error("No character card found in PNG file")
}

private fun ByteString.extractTextChunks(): List<PngChunkText> {
    val byteReader = ByteArrayByteReader(toByteArray())
    val chunks = PngImageParser
        .readChunks(byteReader, listOf(PngChunkType.TEXT))
        .map { it as PngChunkText }
    return chunks
}

private fun PngChunkText.extractChunkText(): String {
    return Base64.decode(text).decodeToString()
}
