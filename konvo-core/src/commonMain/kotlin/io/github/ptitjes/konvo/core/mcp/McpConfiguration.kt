package io.github.ptitjes.konvo.core.mcp

import io.ktor.client.request.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*
import kotlin.time.*

@Serializable
data class McpConfiguration(
    val servers: Map<String, ServerSpecification>? = null,
    val toolPermissions: ToolPermissions? = null,
)

@Serializable
data class ServerSpecification(
    val transport: TransportSpecification,
    val process: ProcessSpecification? = null,
)

@Serializable
sealed interface TransportSpecification {
    @Serializable
    @SerialName("stdio")
    data object Stdio : TransportSpecification

    @Serializable
    @SerialName("sse")
    data class Sse(
        val url: String? = null,
        val reconnectionTime: Duration? = null,
        @Transient
        val requestBuilder: HttpRequestBuilder.() -> Unit = {},
    ) : TransportSpecification
}

@Serializable
data class ProcessSpecification(
    val command: List<String>,
    val environment: Map<String, String>? = null,
)

@Serializable
data class ToolPermissions(
    val default: ToolPermission,
    val rules: List<ToolPermissionRule>? = null,
)

@Serializable
data class ToolPermissionRule(
    @Serializable(with = RegexSerializer::class)
    val pattern: Regex,
    val permission: ToolPermission,
)

@Serializable
enum class ToolPermission {
    @SerialName("allow")
    ALLOW,
    @SerialName("ask")
    ASK,
}

private class RegexSerializer : KSerializer<Regex> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("regex", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Regex) {
        encoder.encodeString(value.pattern)
    }

    override fun deserialize(decoder: Decoder): Regex {
        decoder.decodeString().let { pattern ->
            return Regex(pattern)
        }
    }
}
