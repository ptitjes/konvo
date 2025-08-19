package io.github.ptitjes.konvo.frontend.discord.components

import ai.koog.prompt.markdown.*
import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

fun EphemeralContainerBuilder.mcpServerSelector(
    servers: Set<String>,
    selectedServers: Set<String>?,
    onSelectServers: suspend (Set<String>?) -> Unit,
) {
    textDisplay { content = markdown { bold("Tools:") } }

    actionRow {
        val selectedServers = selectedServers ?: emptySet()

        stringSelect(
            onSelect = { selected ->
                acknowledge()
                onSelectServers(servers.filter { it in selected }.toSet().takeIf { it.isNotEmpty() })
            }
        ) {
            placeholder = "Select some tools"

            servers.forEach { server ->
                option(server, server) {
                    default = server in selectedServers
                }
            }

            allowedValues = 0..servers.size
        }
    }
}
