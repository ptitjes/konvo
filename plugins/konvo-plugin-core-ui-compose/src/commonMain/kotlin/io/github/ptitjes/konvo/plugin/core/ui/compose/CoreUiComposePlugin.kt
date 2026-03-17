package io.github.ptitjes.konvo.plugin.core.ui.compose

import dev.whyoleg.sweetspi.*
import io.github.ptitjes.konvo.plugin.core.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.appearance.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.developer.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.mcp.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.models.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.syrup.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.di.*

@ServiceProvider
object CoreUiComposePlugin : Plugin {

    override val dependencies: Set<Plugin> = setOf(CorePlugin)

    override fun PluginSpecificationBuilder.specification() {
        exposedType<App>()

        extensionPoint(SettingsSections)

        appearanceSettings()
        developerSettingsSection()
        mcpSettingsSection()
        modelsSettingsSection()
    }

    override fun DI.Builder.implementation() {
        bindProviderOf(::SettingsListViewModel)
        bindProviderOf(::SettingsViewModel)
        bindProviderOf(::ConversationListViewModel)
        bindProviderOf(::NewConversationViewModel)
        bindProviderOf(::MainScreenViewModel)
        bindFactory { conversationId: String -> new(::ConversationViewModel, conversationId) }

        bind { singleton { App(di) } }
    }
}
