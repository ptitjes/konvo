package io.github.ptitjes.konvo.experiments.agent

import com.xemantic.ai.tool.schema.meta.*
import io.github.ptitjes.konvo.core.ai.base.*
import io.github.ptitjes.konvo.core.ai.spi.*
import io.github.ptitjes.konvo.core.ai.spi.Format
import kotlinx.coroutines.flow.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

// Conclusion of this experiment:

// Use this recursive decomposition principle to:
// 1. decompose the problem domain into smaller problems
// 2. for each sub-problem, create a task and decompose it into sub-tasks
// 3. execute each sub-tasks

// That's a materialization of the waterfall methodology
// \- Specify user reqs.               /- Test adherence to user reqs.
//  \- Specify functional reqs.       /- Test adherence to func. reqs.
//   \- Specify sw. design           /- Test adherence to sw. design decisions
//    \- Implement -----------------/- Test individual units

class Planner(
    val modelProvider: ModelProvider,
) {
    lateinit var modelCard: ModelCard

    suspend fun initialize() {
        val modelCards = modelProvider.queryModelCards()
        modelCard = modelCards.first { it.name == MODEL_NAME }
    }

    suspend fun buildPlan(
        prompt: String,
        maxDepth: Int = 3,
    ): PlanNode {
        return decompose(listOf(), prompt, maxDepth)
    }

    private suspend fun decompose(
        parentSteps: List<PlanNode>,
        prompt: String,
        maxDepth: Int,
    ): PlanNode {
        println("Decomposing:")
        println(prompt)
        println()

        val decomposed = buildIntermediatePlan(prompt)
        println("Decomposed: $decomposed")
        println()

        return PlanNode(
            goal = decomposed.goal,
            description = decomposed.description,
            steps = decomposed.steps.map { leafStep ->
                decomposeSubTask(parentSteps + decomposed, leafStep, maxDepth)
            }
        )
    }

    private suspend fun decomposeSubTask(
        parentSteps: List<PlanNode>,
        step: PlanNode,
        maxDepth: Int,
    ): PlanNode {
        if (parentSteps.size >= maxDepth - 1) return step
        val prompt = buildSubTaskPrompt(parentSteps, step)
        return decompose(parentSteps + step, prompt, maxDepth)
    }

    private fun buildSubTaskPrompt(
        parentSteps: List<PlanNode>,
        step: PlanNode,
    ): String {
        return buildString {
            appendLine("We have already decomposed a bigger plan in smaller steps.")
            appendLine("Here are the parent steps:")
            parentSteps.forEachIndexed { index, step ->
                val indent = "  ".repeat(index)
                appendLine("$indent - ${step.goal}")
                appendLine("$indent   Description: ${step.goal}")
            }

            appendLine()
            appendLine("Now, decompose this sub-step:")
            appendLine("Goal: ${step.goal}")
            appendLine("Description: ${step.description}")
        }
    }

    private suspend fun buildIntermediatePlan(prompt: String): PlanNode {
        val message = newPlannerBot().chat(ChatMessage.User(prompt)).last()
        if (message !is ChatMessage.Assistant || message.text.isBlank())
            error("Failed to build sub-plan for prompt:\n$prompt")
        return Json.decodeFromString<IntermediatePlan>(message.text).toPlanNode()
    }

    private fun newPlannerBot(): ChatBot = ChatBot(modelCard) {
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
                                You are an helpful AI agent and an expert in software development planning.
                                Devise a detailed plan to realize the task defined by the user.
                                If you think that the task is sufficiently easy to be done in one step, then define an empty list of steps.
                            """.trimIndent()
                )
            )
        }

        format {
            Format.FormatSchema(jsonSchemaOf<IntermediatePlan>())
        }
    }

    companion object {
        private const val MODEL_NAME = "hf.co/mradermacher/ToolACE-2-Llama-3.1-8B-i1-GGUF:Q4_K_M"
    }
}

@Serializable
data class PlanNode(
    @Description("A short title for the goal of this plan.")
    val goal: String,
    @Description("A description for this plan.")
    val description: String,
    @Description("The list of steps that composes this plan, or an empty list if this plan can be achieved individually.")
    @ItemDescription("Describes a step in fulfilling the plan.")
    val steps: List<PlanNode> = listOf(),
)

@Serializable
@Description("A plan to fulfill the task at hand.")
private data class IntermediatePlan(
    @Description("A short title for the goal of this plan.")
    val goal: String,
    @Description("A description for this plan.")
    val description: String,
    @Description("The list of steps that composes this plan.")
    @ItemDescription("Describes a step in fulfilling the plan.")
    val steps: List<IntermediateStep>,
) {
    fun toPlanNode(): PlanNode = PlanNode(
        goal = goal,
        description = description,
        steps = steps.map { it.toPlanNode() },
    )
}

@Serializable
@Description("A step that is part of a plan.")
private data class IntermediateStep(
    @Description("A short title for the step.")
    val goal: String,
    @Description("A description for this step.")
    val description: String
) {
    fun toPlanNode(): PlanNode = PlanNode(
        goal = goal,
        description = description,
    )
}
