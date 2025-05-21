package io.github.ptitjes.konvo

import io.github.ptitjes.konvo.backend.ollama.*
import io.github.ptitjes.konvo.experiments.agent.*
import kotlinx.io.*
import kotlinx.io.files.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

suspend fun runExperiments() {
    val configuration = KonvoAppConfiguration.readConfiguration(Path("config/konvo.json"))
    val modelProvider = OllamaProvider(configuration.ollama.url)

    val prompt = """
            Implement a simple clock application.
            
            ### Features
            - displays a clock (both analogic and digital)
            - allows to define alarms
            - emits a notification when an alarm chimes
            
            ### Technology stack:
            - TypeScript with strict type-checking (no JavaScript files)
            - Electron
            - React, with functional components
            - Vitest
            - Sqlite, for the persistence of the alarms
        """.trimIndent()

    // generateGuidelines(modelProvider, prompt)

    // generatePlan(modelProvider, prompt)
}

private suspend fun generateGuidelines(modelProvider: OllamaProvider, prompt: String) {
    val agent = Guidelines(modelProvider)
    agent.initialize()

    val guidelines = agent.generateGuidelines(prompt)
    println(guidelines)

    SystemFileSystem.sink(Path("guidelines.md")).buffered().use { sink ->
        sink.writeString(guidelines)
    }
}

private suspend fun generatePlan(modelProvider: OllamaProvider, prompt: String) {
    val agent = Planner(modelProvider)
    agent.initialize()

    val plan = agent.buildPlan(prompt)

    val planString = prettyJson.encodeToString(plan)
    println(planString)

    SystemFileSystem.sink(Path("plan.json")).buffered().use { sink ->
        sink.writeString(planString)
    }
}

private val prettyJson = Json {
    prettyPrint = true
}
