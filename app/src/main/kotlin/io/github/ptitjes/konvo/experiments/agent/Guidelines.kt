package io.github.ptitjes.konvo.experiments.agent

import io.github.ptitjes.konvo.core.ai.base.*
import io.github.ptitjes.konvo.core.ai.spi.*
import kotlinx.coroutines.flow.*

// Conclusion of this experiment:

// - use RAG in order to provide sample guidelines for the technology stack used

class Guidelines(
    val modelProvider: ModelProvider,
) {
    lateinit var modelCard: ModelCard

    suspend fun initialize() {
        val modelCards = modelProvider.queryModelCards()
        modelCard = modelCards.first { it.name == MODEL_NAME }
    }

    suspend fun generateGuidelines(prompt: String): String {
        val message = newGuidelineBot().chat(
            ChatMessage.User(
                """
                    Here is the task you need to write guidelines for:
                    
                    $prompt
                """.trimIndent()
            )
        ).last()
        if (message !is ChatMessage.Assistant || message.text.isBlank())
            error("Failed to build sub-plan for prompt:\n$prompt")
        return message.text
    }

    private fun newGuidelineBot(): ChatBot = ChatBot(modelCard) {
        chatMemory {
            DefaultChatMemory(
                memoryStore = InMemoryChatMemoryStore(),
                evictionStrategy = TokenWindowEvictionStrategy(
                    maxTokenCount = modelCard.contextSize!!.toInt()
                ),
            )
        }

        prompt {
            listOf(
                ChatMessage.System(
                    """
                            You are an helpful AI agent and an expert in software development best-practices.
                            Write software guidelines that are suitable for the technologies used in the task defined by the user.
                            They will be used to asses the quality of the code produced by the coding agent.
                            
                            These guidelines can include:
                            - project structure,
                            - general code organization principles,
                            - file-naming and coding conventions
                              - for each programming language and framework used, if they use different conventions
                            - testing,
                            - build tools used to check and enforce the guidelines.
                            
                            Use Markdown format with headers for each sections.
                            Use a main header to name the project guidelines.
                            Do NOT include a sentence at the start or a sentence at the end.
                        """.trimIndent()
                )
            )
        }
    }

    companion object {
        private const val MODEL_NAME = "hf.co/mradermacher/ToolACE-2-Llama-3.1-8B-i1-GGUF:Q4_K_M"
    }
}
