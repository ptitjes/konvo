package io.github.ptitjes.konvo

import ai.koog.prompt.dsl.*
import ai.koog.prompt.executor.ollama.client.*
import ai.koog.prompt.llm.*
import ai.koog.prompt.markdown.*
import kotlinx.coroutines.*

suspend fun main() = coroutineScope {
    val client = OllamaClient()

    val models = mapOf(
        "granite3.2-vision:2b" to client.getModelOrNull("granite3.2-vision:2b")!!.toLLModel(),
        "gemma3:4b" to client.getModelOrNull("gemma3:4b")!!.toLLModel(),
        "llama3.2-vision:11b" to client.getModelOrNull("llama3.2-vision:11b")!!.toLLModel(),
    )

    val images = mapOf(
        "Kotlin logo" to "/home/didier/Pictures/kotlin-logo.jpg",
        "Avatar" to "/home/didier/Pictures/Avatar.png",
        "Avatar Veganism" to "/home/didier/Pictures/Avatar-Veganism.png",
        "Chasseur" to "/home/didier/Pictures/petition-chasse.jpg",
        "Nupes" to "/home/didier/Pictures/NUPES/logo-nupes-complet.png",
        "Borne" to "/home/didier/Pictures/Facebook/borne.jpg",
        "French Polynesia" to "/home/didier/Pictures/Wallpapers/french-polynesia-1.jpg",
    )

    val results = models.mapValues { (modelName, model) ->
        images.mapValues { (imageName, image) ->
            println("- $modelName - $imageName")
            client.queryImageContent(image, model)
        }
    }
    println()
    println()

    val reversedResults = results
        .flatMap { (model, modelResults) -> modelResults.entries.map { (image, result) -> image to (model to result) } }
        .groupBy { (image, _) -> image }
        .mapValues { (_, imageResults) -> imageResults.associate { (_, imageResult) -> imageResult } }

    println(
        markdown {
            for ((image, imageResults) in reversedResults) {
                h1(image)
                newline()

                for ((model, result) in imageResults) {
                    h2(model)
                    newline()
                    +result
                    newline()
                }
                br()
            }
        }
    )

}

private suspend fun OllamaClient.queryImageContent(
    image: String,
    model: LLModel
): String {
    val prompt = prompt("test") {
        user {
            text("Please describe this image.")
            attachments { image(image) }
        }
    }
    val responses = execute(prompt, model)
    return responses.joinToString("\n") { it.content }
}
