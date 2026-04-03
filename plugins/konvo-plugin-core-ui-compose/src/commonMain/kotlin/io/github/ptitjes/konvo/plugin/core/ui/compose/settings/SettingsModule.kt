package io.github.ptitjes.konvo.plugin.core.ui.compose.settings

import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.*
import org.kodein.di.*

val settingsModule = DI.Module("settings") {
    bindSingletonOf(::SettingsSectionManager)

    inBindSet<Presenter.Factory> {
        addSingleton { new(::SettingsPresenterFactory, di) }
    }
    inBindSet<Ui.Factory> {
        addSingleton { new(::SettingsUiFactory) }
    }
}

private class SettingsPresenterFactory(
    private val di: DI,
    private val sectionManager: SettingsSectionManager,
) : Presenter.Factory {
    override fun create(screen: Screen, navigator: Navigator, context: CircuitContext): Presenter<*>? {
        val settingsNavigator = SettingsNavigator(navigator)

        return when (screen) {
            is SettingsListScreen -> di.direct.newInstance {
                new(::SettingsListPresenter, settingsNavigator)
            }

            is SettingsScreen -> di.direct.newInstance {
                new(::SettingsSectionPresenter, a1 = screen, a2 = settingsNavigator)
            }

            is SettingsSectionView -> sectionManager.sectionForKey(screen.key)
                .presenterFactory(settingsNavigator)

            else -> null
        }
    }
}

private class SettingsUiFactory(
    private val sectionManager: SettingsSectionManager,
) : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is SettingsListScreen -> ui(::SettingsListScreen)
            is SettingsScreen -> ui(::SettingsSectionScreen)
            is SettingsSectionView -> uiForSection(sectionManager.sectionForKey(screen.key))
            else -> null
        }
    }
}

private fun <S : SettingsSectionState> uiForSection(section: SettingsSection<S>): Ui<S> {
    return ui { state, modifier -> section.panel(state) }
}