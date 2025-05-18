package io.github.ptitjes.konvo.frontend.discord.components

import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.ai.spi.Tool
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

fun EphemeralContainerBuilder.toolSelector(
    tools: List<Tool>,
    selectedTools: List<Tool>,
    onSelectTools: suspend (List<Tool>) -> Unit,
) {
    textDisplay { content = "**Tools:**" }

    actionRow {
        val selectedToolNames = selectedTools.map { it.name }

        stringSelect(
            onSelect = { selected ->
                acknowledge()
                onSelectTools(tools.filter { it.name in selected })
            }
        ) {
            placeholder = "Select some tools"

            tools.forEach { tool ->
                option(tool.name, tool.name) {
                    description = tool.description
                        .replace('\n', ' ').maybeEllipsisDiscordLabel()
                    default = tool.name in selectedToolNames
                }
            }

            allowedValues = 0..tools.size
        }
    }
}
