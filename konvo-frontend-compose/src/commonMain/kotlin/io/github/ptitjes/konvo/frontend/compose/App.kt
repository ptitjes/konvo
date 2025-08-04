package io.github.ptitjes.konvo.frontend.compose

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.conversation.*
import io.github.ptitjes.konvo.frontend.compose.components.*

@Composable
fun App(
    konvo: Konvo,
) {
    MaterialTheme(
        colorScheme = darkColorScheme(),
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            var conversation by remember { mutableStateOf<Conversation?>(null) }

            LaunchedEffect(Unit) {
                conversation = konvo.createDummyConversation()
            }

            val conversationView = remember(conversation) { conversation?.newUiView() }
            val viewModel = remember(conversationView) {
                conversationView?.let { ConversationViewModel(it) }
            }

            if (viewModel != null) {
                ConversationPane(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

private suspend fun Konvo.createDummyConversation(): Conversation {
    val prompt = prompts.first { it.name == "question-and-answer" }
    val tools = tools.filter { it.name.startsWith("web_") }
    val model = models.first { it.name.contains("ToolACE-2") }

    return createConversation(
        configuration = ConversationConfiguration(
            agent = QuestionAnswerAgentConfiguration(
                prompt = prompt,
                tools = tools,
                model = model,
            )
        )
    )
}
