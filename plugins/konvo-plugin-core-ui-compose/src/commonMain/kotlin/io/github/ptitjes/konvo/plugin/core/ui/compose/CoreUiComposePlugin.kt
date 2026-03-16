package io.github.ptitjes.konvo.plugin.core.ui.compose

import dev.whyoleg.sweetspi.*
import io.github.ptitjes.konvo.plugin.core.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.conversations.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import io.github.ptitjes.syrup.*
import io.github.ptitjes.syrup.specification.*
import org.kodein.di.*

@ServiceProvider
object CoreUiComposePlugin : Plugin {

    override val dependencies: Set<Plugin> = setOf(_root_ide_package_.io.github.ptitjes.konvo.plugin.core.CorePlugin)

    override fun PluginSpecificationBuilder.specification() {
        exposedType<App>()

        extensionPoint(SettingsSections)
    }

    override fun DI.Builder.implementation() {
        bindProviderOf(::SettingsListViewModel)
        bindProviderOf(::SettingsViewModel)
        bindProviderOf(::ConversationListViewModel)
        bindProviderOf(::NewConversationViewModel)
        bindProviderOf(::MainScreenViewModel)
        bindFactory { conversationId: String -> new(::ConversationViewModel, conversationId) }

        bind { singleton { App(di) }}
    }
}
