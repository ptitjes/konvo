package io.github.ptitjes.konvo.plugin.roleplay

import dev.whyoleg.sweetspi.*
import io.github.ptitjes.konvo.plugin.core.*
import io.github.ptitjes.syrup.*

@ServiceProvider
object RoleplayPlugin : Plugin {
    override val id: PluginId = PluginConfig.Id

    override val dependencies: Set<Plugin> = setOf(CorePlugin)
}
