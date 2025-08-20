package io.github.ptitjes.konvo.core.roleplay.providers

import com.ashampoo.kim.format.png.*
import com.ashampoo.kim.format.png.chunk.*
import com.ashampoo.kim.input.*
import io.github.ptitjes.konvo.core.roleplay.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.io.*
import kotlinx.io.bytestring.*
import kotlinx.io.files.*
import kotlinx.serialization.json.*
import kotlin.io.encoding.*

fun ByteString.extractCharacterCard(id: String): CharacterCard {
    val chunks = extractTextChunks()
    val ccv3Chunk = chunks.firstOrNull { it.getKeyword() == "ccv3" }
    if (ccv3Chunk != null) return ccv3Chunk.extractJson().parseCharacterCard(id)

    val charaChunk = chunks.firstOrNull { it.getKeyword() == "chara" }
    if (charaChunk != null) return charaChunk.extractJson().parseCharacterCard(id)

    error("Unknown character format")
}

private fun PngChunkText.extractChunkText(): String {
    return Base64.decode(text).decodeToString()
}

private fun PngChunkText.extractJson(): JsonObject {
    return Json.decodeFromString(extractChunkText())
}

private fun ByteString.extractTextChunks(): List<PngChunkText> {
    val byteReader = ByteArrayByteReader(toByteArray())
    val chunks = PngImageParser
        .readChunks(byteReader, listOf(PngChunkType.TEXT))
        .map { it as PngChunkText }
    return chunks
}

fun main() {
    val prettyJson = Json { prettyPrint = true }
    val bytes = SystemFileSystem.readBytes(Path("/home/didier/Downloads/main_jacklyn-08d3af328ca2_spec_v2.png"))
    val chunks = bytes.extractTextChunks()
    chunks.forEachIndexed { index, chunk ->
        SystemFileSystem.sink(Path("./chunk-$index.json")).buffered().use { sink ->
            sink.writeString(prettyJson.encodeToString(chunk.extractJson()))
        }
    }
}
