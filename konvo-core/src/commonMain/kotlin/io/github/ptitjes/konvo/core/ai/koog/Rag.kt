package io.github.ptitjes.konvo.core.ai.koog

import ai.koog.agents.core.dsl.builder.*
import ai.koog.prompt.markdown.*
import ai.koog.rag.base.*
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList

fun <Document> AIAgentSubgraphBuilderBase<*, *>.nodeNaiveRagRetrieve(
    name: String? = null,
    storage: RankedDocumentStorage<Document>,
    count: Int = Int.MAX_VALUE,
    similarityThreshold: Double = 0.0,
) = node<String, AugmentedQuery<Document>>(name) { query ->
    val relevantDocuments = storage.fixedMostRelevantDocuments(query, count, similarityThreshold).take(count)
    println("=== using ${relevantDocuments.size} documents")
    AugmentedQuery(query = query, augmentation = relevantDocuments)
}

suspend fun <Document> RankedDocumentStorage<Document>.fixedMostRelevantDocuments(
    query: String,
    count: Int = Int.MAX_VALUE,
    similarityThreshold: Double = 1.0
): Iterable<Document> = rankDocuments(query)
    .filter { it.similarity <= similarityThreshold }
    .toList()
    .sortedBy { it.similarity }
    .take(count)
    .map { it.document }
    .toList()

data class AugmentedQuery<Document>(
    val query: String,
    val augmentation: List<Document>,
)

fun <Document> AIAgentSubgraphBuilderBase<*, *>.nodeNaiveRagAugmentQuery(
    name: String? = null,
    source: (Document) -> String,
    content: (Document) -> String,
) = node<AugmentedQuery<Document>, String>(name) { query ->
    markdown {
        text(query.query)

        val augmentation = query.augmentation
        if (augmentation.isNotEmpty()) {
            br()

            text("Answer the above query using the following resources:")
            br()

            augmentation.forEach { document ->
                println("=== using: ${source(document)}")

                line { bold("source:"); text(source(document)) }
                line { bold("content:") }
                text(content(document))
                br()
            }
        }
    }
}
