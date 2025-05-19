package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.ai.base.*
import io.github.ptitjes.konvo.core.ai.spi.*
import kotlinx.coroutines.*
import kotlinx.datetime.*
import kotlinx.datetime.format.*

class QuestionAnswerConversation(
    coroutineScope: CoroutineScope,
    override val configuration: QuestionAnswerModeConfiguration,
) : TurnBasedConversation(coroutineScope) {
    override fun buildChatBot() = ChatBot(configuration.modelCard) {
        val contextSize = configuration.modelCard.contextSize?.toInt()
        val evictionStrategy = if (contextSize != null) TokenWindowEvictionStrategy(contextSize)
        else MessageWindowEvictionStrategy(20)

        chatMemory {
            DefaultChatMemory(
                memoryStore = InMemoryChatMemoryStore(),
                evictionStrategy = evictionStrategy
            )
        }

        prompt { listOf(ChatMessage.System(text = buildSystemPrompt())) }

        tools { configuration.tools }
    }

    fun buildSystemPrompt(): String = buildString {
        appendLine(
            """
            You are a helpful assistant and an expert in function composition.
            You can answer general questions using your internal knowledge OR invoke functions when necessary.
            Only use tools if you really need to. When in doubt, ask the user.
            If you use your internal knowledge, tell the user.
        """.trimIndent()
        )
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.format(dateFormat)
        appendLine("Today Date: $today")
    }

    val dateFormat = LocalDate.Format {
        dayOfMonth()
        char(' ')
        monthName(MonthNames.ENGLISH_FULL)
        char(' ')
        year()
    }
}
