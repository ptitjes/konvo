package io.github.ptitjes.konvo

import ai.koog.embeddings.base.*
import ai.koog.embeddings.local.*
import ai.koog.prompt.executor.ollama.client.*
import ai.koog.prompt.llm.*
import ai.koog.rag.base.*
import ai.koog.rag.vector.*
import io.github.ptitjes.konvo.core.*
import io.github.ptitjes.konvo.core.ai.koog.*
import io.github.ptitjes.konvo.core.ai.mcp.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.conversation.*
import kotlinx.coroutines.*
import kotlinx.io.*
import kotlinx.io.files.*

suspend fun main() = coroutineScope {
    val configuration = KonvoAppConfiguration.readConfiguration(Path("config/konvo.json5"))

    val mcpServersManager = McpServersManager(
        coroutineContext = coroutineContext,
        specifications = configuration.mcp.servers,
    )

    try {
        mcpServersManager.startAndConnectServers()

        val konvo = startKonvo {
            dataDirectory = configuration.dataDirectory

            configuration.modelProviders.forEach { (name, configuration) ->
                installModels(
                    when (configuration) {
                        is ModelProviderConfiguration.Ollama -> OllamaModelProvider(name, configuration.baseUrl)
                    }
                )
            }

            installPrompts(
                McpPromptProvider(
                    serversManager = mcpServersManager,
                )
            )

            installTools(
                McpToolProvider(
                    serversManager = mcpServersManager,
                    permissions = configuration.mcp.toolPermissions,
                )
            )

            installKnowledgeBase(
                ExperimentalKnowledgeBaseProvider(
                    Path("data/kbs")
                )
            )
        }

//        konvo.discordBot(configuration.discord.token)
        konvo.tempConsole()
    } finally {
        mcpServersManager.disconnectAndStopServers()
    }
}

class ExperimentalKnowledgeBaseProvider(
    val knowledgeBasesPath: Path,
) : KnowledgeBaseProvider {
    override val name: String get() = "experimental"

    override suspend fun queryKnowledgeBases(): List<KnowledgeBaseCard> {
        val knowledgeBase = ExperimentalKnowledgeBase("mcp", Path(knowledgeBasesPath, "mcp"))
        knowledgeBase.load()
        return listOf(knowledgeBase)
    }

    private inner class ExperimentalKnowledgeBase(
        override val name: String,
        val rootPath: Path,
    ) : KnowledgeBaseCard {
        override val provider get() = this@ExperimentalKnowledgeBaseProvider

        private val embedder = LLMEmbedder(
            client = OllamaClient(),
            model = LLModel(
                provider = LLMProvider.Ollama,
                id = "nomic-embed-text",
                capabilities = listOf(LLMCapability.Embed)
            ),
        )

        private val storage = InMemoryDocumentEmbeddingStorage(
            embedder = object : DocumentEmbedder<MarkdownFragment> {
                override suspend fun embed(document: MarkdownFragment): Vector =
                    embedder.embed(document.content)

                override suspend fun embed(text: String): Vector =
                    embedder.embed(text)

                override fun diff(embedding1: Vector, embedding2: Vector): Double =
                    embedder.diff(embedding1, embedding2)
            },
        )

        suspend fun load() {
            var totalFragmentCount = 0
            val paths = SystemFileSystem.list(rootPath).filter { it.name.endsWith(".md") }
            for (path in paths) {
                println("=== processing $path")
                SystemFileSystem.source(path).buffered().use { source ->
                    val fragments = source.splitMarkdown(path, 1000)
                    for (fragment in fragments) {
                        storage.store(fragment)
                        totalFragmentCount++
                    }
                }
            }
            println("=== $totalFragmentCount fragments added")
        }

        override fun getRankedDocumentStorage(): RankedDocumentStorage<MarkdownFragment> {
            return storage
        }
    }
}

private fun Source.splitMarkdown(
    path: Path,
    maxFragmentSize: Int,
): Sequence<MarkdownFragment> = sequence {
    var index = 0
    var currentFragmentBuilder: StringBuilder? = null
    var currentLine: String? = null

    while (true) {
        if (currentFragmentBuilder == null) currentFragmentBuilder = StringBuilder()

        currentLine = readLine()
        if (currentLine == null) break

        if (currentFragmentBuilder.length + currentLine.length > maxFragmentSize) {
            val content = currentFragmentBuilder.toString()
            val fragment = MarkdownFragment(
                path = path,
                content = content,
                startIndex = index,
                endIndex = index + content.length,
            )
            currentFragmentBuilder = null
            index += content.length

            yield(fragment)

            continue
        }

        currentFragmentBuilder.appendLine(currentLine)
    }
}

suspend fun Konvo.tempConsole() {
    val prompt = prompts.first { it.name == "question-and-answer" }

    val knowledgeBase = knowledgeBases.first { it.name == "mcp" }
    val model = models.first { it.name == "hf.co/mradermacher/ToolACE-2-Llama-3.1-8B-i1-GGUF:Q4_K_M" }

    val conversation = createConversation(
        ConversationConfiguration(
            mode = QuestionAnswerModeConfiguration(
                prompt = prompt,
                tools = listOf(),
                knowledgeBase = knowledgeBase,
                model = model,
            )
        )
    )

    while (true) {
        print("User: ")
        val query = readln()
        if (query == "quit") break

        conversation.userEvents.send(query)

        for (assistantEvent in conversation.assistantEvents) {
            when (assistantEvent) {
                is AssistantEvent.Message -> {
                    println("Assistant: ${assistantEvent.content}")
                    break
                }

                AssistantEvent.Processing -> println("Assistant is processing...")
                else -> TODO()
            }
        }
    }
}
