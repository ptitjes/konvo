package io.github.ptitjes.konvo.core.ai.spi

sealed interface ToolCallResult {
    data class Success(val text: String) : ToolCallResult
    data class ExecutionFailure(val reason: String) : ToolCallResult
}
