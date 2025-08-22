package io.github.ptitjes.konvo.core.conversations.model

sealed interface ToolCallResult {
    data class Success(val text: String) : ToolCallResult
    data class ExecutionFailure(val reason: String) : ToolCallResult
}
