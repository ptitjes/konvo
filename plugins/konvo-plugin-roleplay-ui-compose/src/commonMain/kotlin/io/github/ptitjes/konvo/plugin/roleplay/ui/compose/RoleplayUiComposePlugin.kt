package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import dev.whyoleg.sweetspi.*
import io.github.ptitjes.konvo.plugin.core.*
import io.github.ptitjes.konvo.plugin.core.i18n.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.i18n.*
import io.github.ptitjes.konvo.plugin.roleplay.*
import io.github.ptitjes.konvo.plugin.roleplay.ui.compose.i18n.*
import io.github.ptitjes.syrup.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.di.*

@ServiceProvider
object RoleplayUiComposePlugin : Plugin {
    override val id: PluginId = PluginConfig.Id

    override val dependencies: Set<Plugin> = setOf(CorePlugin, CoreUiComposePlugin, RoleplayPlugin)

    override fun PluginSpecificationBuilder.specification() {
        i18nStrings<RoleplayStrings>("ar-SA") { ArStrings }
        i18nStrings<RoleplayStrings>("en-US") { EnStrings }
        i18nStrings<RoleplayStrings>("es-ES") { EsStrings }
        i18nStrings<RoleplayStrings>("fr-FR") { FrStrings }
        i18nStrings<RoleplayStrings>("hi-IN") { HiStrings }
        i18nStrings<RoleplayStrings>("zh-CN") { ZhStrings }

        roleplaySettingsSections()

        AgentConfigurationPanes {
            contribution {
                AgentConfigurationPane(
                    label = { i18n.roleplay.agentDisplayName },
                    presenterFactory = { new(::RoleplayConfigurationPresenter, it) },
                    panel = { state, modifier ->
                        RoleplayConfigurationPanel(
                            state = state,
                            modifier = modifier,
                        )
                    }
                )
            }
        }
    }
}
