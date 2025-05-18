package io.github.ptitjes.konvo.core.ai.spi

sealed interface ToolCallResult {
    data class Success(val text: String) : ToolCallResult
    data object NoSuchTool : ToolCallResult
    data object NotAllowed : ToolCallResult
    data class ExecutionFailure(val reason: String) : ToolCallResult
}

fun ToolCallResult.contentString(call: ToolCall): String = when (this) {
    ToolCallResult.NoSuchTool -> "No such tool: ${call.name}"
    ToolCallResult.NotAllowed -> "Unauthorized tool call: ${call.name}"
    is ToolCallResult.ExecutionFailure -> "Tool execution failed: $reason"
    is ToolCallResult.Success -> text
}
