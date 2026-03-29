package io.github.ptitjes.konvo.plugin.core.ui.compose.conversations

import com.slack.circuit.runtime.*
import com.slack.circuit.runtime.presenter.*
import com.slack.circuit.runtime.screen.Screen
import com.slack.circuit.runtime.ui.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.*
import io.github.ptitjes.konvo.plugin.core.ui.compose.agents.configuration.*
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
        val conversationNavigator = ConversationNavigator(navigator)

        return when (screen) {
            is ConversationListScreen -> di.direct.newInstance {
                new(::ConversationListPresenter, conversationNavigator)
            }

            is AgentConfigurationScreen -> di.direct.newInstance {
                new(::AgentConfigurationPresenter, conversationNavigator)
            }

            else -> null
        }
    }
}

private class ConversationsUiFactory : Ui.Factory {
    override fun create(screen: Screen, context: CircuitContext): Ui<*>? {
        return when (screen) {
            is ConversationListScreen -> ui(::ConversationList)
            is AgentConfigurationScreen -> ui(::AgentConfigurationScreen)
            else -> null
        }
    }
}
