package io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.mcp.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.widgets.*

@Composable
internal fun QuestionAnswerConfigurationPanel(
    modifier: Modifier,
    state: QuestionAnswerConfigurationView.State,
    onGoToSettingsClick: (titleKey: String) -> Unit,
) {
    when (state) {
        QuestionAnswerConfigurationView.State.Loading -> FullSizeProgressIndicator(modifier = modifier)
        is QuestionAnswerConfigurationView.State.Available -> Column(modifier = modifier) {
            McpServerSelector(
                selectedServers = state.selectedMcpServers,
                onServersSelected = {
                    state.eventSink(QuestionAnswerConfigurationView.Event.SelectMcpServerNames(it))
                },
                servers = state.availableMcpServers,
            )

            if (state.availableModels.isEmpty()) {
                UnavailabilityPlaceholder(
                    unavailabilityText = i18n.conversations.qaNoModels,
                    onGoToSettings = { onGoToSettingsClick("models") },
                )
            } else {
                ModelSelector(
                    selectedModel = state.selectedModel ?: state.availableModels.first(),
                    onModelSelected = {
                        state.eventSink(QuestionAnswerConfigurationView.Event.SelectModel(it))
                    },
                    models = state.availableModels,
                )
            }
        }
    }
}