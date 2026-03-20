package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import androidx.compose.runtime.*
import com.slack.circuit.retained.*
import com.slack.circuit.runtime.presenter.*
import io.github.ptitjes.konvo.plugin.core.mcp.*
import io.github.ptitjes.konvo.plugin.core.models.*

class QuestionAnswerConfigurationPresenter(
    private val modelManager: ModelManager,
    private val mcpServerSpecificationsManager: McpServerSpecificationsManager,
) : Presenter<QuestionAnswerConfigurationView.State> {
    @Composable
    override fun present(): QuestionAnswerConfigurationView.State {
        val models by modelManager.models.collectAsState(null)
        val mcpServerSpecifications by mcpServerSpecificationsManager.specifications.collectAsState(null)

        val availableModels = models
        val availableMcpServers = mcpServerSpecifications

        return if (availableModels == null || availableMcpServers == null) {
            QuestionAnswerConfigurationView.State.Loading
        } else {
            var selectedModel by rememberRetained { mutableStateOf(availableModels.firstOrNull()) }
            var selectedMcpServers by rememberRetained { mutableStateOf(emptySet<String>()) }

            val selectableModels = if (selectedMcpServers.isNotEmpty()) {
                availableModels.filter { it.supportsTools }
            } else {
                availableModels
            }

            QuestionAnswerConfigurationView.State.Available(
                availableModels = selectableModels,
                availableMcpServers = availableMcpServers.keys,
                selectedModel = selectedModel,
                selectedMcpServers = selectedMcpServers,
            ) { event ->
                when (event) {
                    is QuestionAnswerConfigurationView.Event.SelectModel -> {
                        selectedModel = event.model
                    }

                    is QuestionAnswerConfigurationView.Event.SelectMcpServerNames -> {
                        selectedMcpServers = event.mcpServerNames
                        val alreadySupportsTools = selectedModel?.supportsTools ?: false
                        selectedModel =
                            if (selectedMcpServers.isEmpty() || alreadySupportsTools) availableModels.firstOrNull()
                            else null
                    }
                }
            }
        }
    }
}
