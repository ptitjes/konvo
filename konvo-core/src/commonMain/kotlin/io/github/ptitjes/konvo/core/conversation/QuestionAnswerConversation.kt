package io.github.ptitjes.konvo.core.conversation

import io.github.ptitjes.konvo.core.*
import kotlinx.coroutines.*
import kotlinx.datetime.*
import kotlinx.datetime.format.*

class QuestionAnswerConversation(
    coroutineScope: CoroutineScope,
    override val configuration: QuestionAnswerModeConfiguration,
) : TurnBasedConversation(coroutineScope) {
    override fun buildModel() = buildModel(configuration.model) {
        tools(configuration.tools)
    }

    override fun buildSystemPrompt(): String = buildString {
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
