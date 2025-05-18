package io.github.ptitjes.konvo.tool.web.utils

import io.modelcontextprotocol.kotlin.sdk.*
import kotlinx.serialization.json.*

@Suppress("FunctionName")
inline fun <reified T> JsonContent(value: T): TextContent {
    return TextContent(Json.encodeToString(value))
}
