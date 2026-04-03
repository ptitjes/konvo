package io.github.ptitjes.konvo.plugin.core.ui.compose

import com.slack.circuit.foundation.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.ui.*
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
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.toolkit.text.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.tools.*
import io.github.ptitjes.syrup.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.di.*

@ServiceProvider
object CoreUiComposePlugin : Plugin {
    override val id: PluginId = PluginConfig.Id

    override val dependencies: Set<Plugin> = setOf(CorePlugin)

    override fun PluginSpecificationBuilder.specification() {
        exposedType<App>()

        extensionPoint(SettingsSections)

        extensionPoint(AgentConfigurationPanes)

        AgentConfigurationPanes {
            contribution {
                AgentConfigurationPane(
                    label = { i18n.agents.questionAnswerDisplayName },
                    presenterFactory = { new(::QuestionAnswerConfigurationPresenter, it) },
                    panel = { state, modifier ->
                        QuestionAnswerConfigurationPanel(
                            state = state,
                            modifier = modifier,
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
        bindSet<Presenter.Factory>()
        bindSet<Ui.Factory>()

        import(settingsModule)
        import(conversationsModule)

        bindSingleton<Circuit> {
            buildCircuit(
                presenterFactories = instance(),
                uiFactories = instance(),
            )
        }

        bind { singleton { App(di) } }
    }
}

private fun buildCircuit(
    presenterFactories: Set<Presenter.Factory>,
    uiFactories: Set<Ui.Factory>,
): Circuit {
    println("Building circuit")
    return Circuit.Builder()
        .addPresenterFactories(presenterFactories.also { println("Presenter factories: ${it.size}") })
        .addUiFactories(uiFactories).also { println("UI factories: ${uiFactories.size}") }
        .build()
}
