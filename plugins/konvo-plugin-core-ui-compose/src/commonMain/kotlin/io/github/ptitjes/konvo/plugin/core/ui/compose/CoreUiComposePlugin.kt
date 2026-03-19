package io.github.ptitjes.konvo.plugin.core.ui.compose

import dev.whyoleg.sweetspi.*
import io.github.ptitjes.konvo.plugin.core.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.appearance.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.developer.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.mcp.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.prompts.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.roleplay.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.text.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.tools.*
import io.github.ptitjes.syrup.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.di.*

@ServiceProvider
object CoreUiComposePlugin : Plugin {

    override val dependencies: Set<Plugin> = setOf(CorePlugin)

    override fun PluginSpecificationBuilder.specification() {
        exposedType<App>()

        extensionPoint(SettingsSections)

        extensionPoint(AgentConfigurationPanes)

        AgentConfigurationPanes {
            contribution {
                AgentConfigurationPane(
                    label = { i18n.agents.questionAnswerDisplayName },
                    presenterFactory = { new(::QuestionAnswerConfigurationPresenter) },
                    panel = { state, modifier ->
                        QuestionAnswerConfigurationPanel(
                            state = state,
                            modifier = modifier,
                            onGoToSettingsClick = { },
                        )
                    }
                )
            }
            contribution {
                AgentConfigurationPane(
                    label = { i18n.roleplay.agentDisplayName },
                    presenterFactory = { new(::RoleplayConfigurationPresenter) },
                    panel = { state, modifier ->
                        RoleplayConfigurationPanel(
                            state = state,
                            modifier = modifier,
                            onGoToSettingsClick = { },
                        )
                    }
                )
            }
        }

        appearanceSettings()
        developerSettings()
        mcpSettingsSection()
        modelsSettingsSection()

        agentStrings()
        conversationStrings()
        formatStrings()
        navigationStrings()
        promptStrings()
        settingsStrings()
        toolStrings()
    }

    override fun DI.Builder.implementation() {
        bindProviderOf(::SettingsListViewModel)
        bindProviderOf(::SettingsViewModel)
        bindProviderOf(::ConversationListViewModel)
        bindProviderOf(::MainScreenViewModel)
        bindFactory { conversationId: String -> new(::ConversationViewModel, conversationId) }

        bind { singleton { App(di) } }
    }
}
