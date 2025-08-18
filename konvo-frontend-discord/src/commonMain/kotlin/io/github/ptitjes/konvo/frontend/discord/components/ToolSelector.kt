package io.github.ptitjes.konvo.frontend.discord.components

import ai.koog.prompt.markdown.*
import dev.kord.rest.builder.component.*
import io.github.ptitjes.konvo.core.tools.*
import io.github.ptitjes.konvo.frontend.discord.toolkit.*

fun EphemeralContainerBuilder.toolSelector(
    tools: List<ToolCard>,
    selectedTools: List<ToolCard>?,
    onSelectTools: suspend (List<ToolCard>?) -> Unit,
) {
    textDisplay { content = markdown { bold("Tools:") } }

    actionRow {
        val selectedToolNames = selectedTools?.map { it.name } ?: emptyList()

        stringSelect(
            onSelect = { selected ->
                acknowledge()
                onSelectTools(tools.filter { it.name in selected }.takeIf { it.isNotEmpty() })
            }
        ) {
            placeholder = "Select some tools"

            tools.forEach { tool ->
                option(tool.name, tool.name) {
                    description = tool.description
                        ?.replace('\n', ' ')
                        ?.maybeEllipsisDiscordLabel()
                        ?: ""
                    default = tool.name in selectedToolNames
                }
            }

            allowedValues = 0..tools.size
        }
    }
}
