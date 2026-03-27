package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import io.github.ptitjes.syrup.*

internal class SettingsSectionManager(
    pluginContext: PluginContext,
) {
    val sections by pluginContext.contributions(SettingsSections)

    val sectionsByKey by lazy {
        sections.toList().flatten().associateBy { it.titleKey }
    }

    val firstSection by lazy { sections.toList().minBy { it.titleKey } }

    private fun List<SettingsSection<*>>.flatten(): List<SettingsSection<*>> = flatMap { section ->
        listOf(section) + section.children.flatten()
    }
}

internal fun SettingsSectionManager.sectionForKey(
    key: String,
): SettingsSection<*> {
    return sectionsByKey[key] ?: error("Settings section not found for key: $key")
}
