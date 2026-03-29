package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.settings.*
import org.kodein.di.*

val conversationsModule = DI.Module("conversations") {
    inBindSet<Presenter.Factory> {
        addSingleton { new(::ConversationsPresenterFactory, di) }
    }
    inBindSet<Ui.Factory> {
        addSingleton { new(::ConversationsUiFactory) }
    }
}

private class ConversationsPresenterFactory(
    private val di: DI,
) : Presenter.Factory {
    override fun create(screen: Screen, navigator: Navigator, context: CircuitContext): Presenter<*>? {
        val conversationNavigator = ConversationNavigator(navigator) {
            TODO()
        }

        return when (screen) {
            is ConversationListScreen -> di.direct.newInstance {
                new(::ConversationListPresenter, conversationNavigator)
            }

            else -> null
        }
    }
}

private class ConversationsUiFactory(
    private val sectionManager: SettingsSectionManager,
) : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is ConversationListScreen -> ui(::ConversationList)
            else -> null
        }
    }
}
