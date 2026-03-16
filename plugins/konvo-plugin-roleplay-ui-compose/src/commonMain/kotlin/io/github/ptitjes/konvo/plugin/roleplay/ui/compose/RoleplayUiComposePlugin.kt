package io.github.ptitjes.konvo.plugin.roleplay.ui.compose

import dev.whyoleg.sweetspi.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.roleplay.*
import io.github.ptitjes.syrup.*
import io.github.ptitjes.syrup.specification.*

@ServiceProvider
object RoleplayUiComposePlugin : Plugin {
    override val dependencies: Set<Plugin> = setOf(CoreUiComposePlugin, RoleplayPlugin)

    override fun PluginSpecificationBuilder.specification() {
        roleplaySettingsSections()
    }
}
