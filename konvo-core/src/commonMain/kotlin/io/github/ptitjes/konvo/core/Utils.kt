package io.github.ptitjes.konvo.core

import kotlinx.io.*
import kotlinx.io.bytestring.ByteString
import kotlinx.io.files.*
import kotlinx.serialization.json.*

internal inline fun <reified T> FileSystem.readJson(path: Path): T = source(path).buffered().use { it.readJson() }

internal fun FileSystem.readBytes(path: Path): ByteString = source(path).buffered().use { it.readByteString() }

internal inline fun <reified T> Source.readJson(): T = Json.Default.decodeFromString<T>(this.readString())
