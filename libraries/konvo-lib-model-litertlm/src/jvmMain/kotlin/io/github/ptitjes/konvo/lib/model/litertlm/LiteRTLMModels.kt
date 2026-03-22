package io.github.ptitjes.konvo.lib.model.litertlm

import ai.koog.prompt.llm.*

object LiteRTLMModels {
    val GEMMA_3_1B = LLModel(
        id = "gemma3-1b-it-int4",
        provider = LiteRTLMProvider,
        capabilities = listOf(
            LLMCapability.Temperature,
        ),
    )

    // Gemma 3n family
    // Input:
    //  - Text string, such as a question, a prompt, or a document to be summarized
    //  - Images, normalized to 256x256, 512x512, or 768x768 resolution and encoded to 256 tokens each
    //  - Audio data encoded to 6.25 tokens per second from a single channel
    //  - Total input context of 32K tokens
    // Output:
    //    - Generated text in response to the input, such as an answer to a question, analysis of image content, or a summary of a document
    //    - Total output length up to 32K tokens, subtracting the request input tokens

    // https://huggingface.co/google/gemma-3n-E2B-it-litert-lm
    val GEMMA_3N_E2B = LLModel(
        id = "gemma-3n-E2B-it-int4",
        provider = LiteRTLMProvider,
        capabilities = listOf(
            LLMCapability.Temperature,
            LLMCapability.Vision.Image,
            LLMCapability.Audio,
        ),
    )

    // https://huggingface.co/google/gemma-3n-E4B-it-litert-lm
    val GEMMA_3N_E4B = LLModel(
        id = "gemma-3n-E4B-it-int4",
        provider = LiteRTLMProvider,
        capabilities = listOf(
            LLMCapability.Temperature,
            LLMCapability.Vision.Image,
            LLMCapability.Audio,
        ),
    )

    // huggingface.co/kontextdev/agent-gemma
    val AGENT_GEMMA = LLModel(
        id = "gemma-3n-E2B-it-agent",
        provider = LiteRTLMProvider,
        capabilities = listOf(
            LLMCapability.Temperature,
            LLMCapability.Vision.Image,
            LLMCapability.Audio,
            LLMCapability.Tools,
        ),
    )

    val FUNCTION_GEMMA_270M = LLModel(
        id = "functiongemma-270m-it",
        provider = LiteRTLMProvider,
        capabilities = listOf(
            LLMCapability.Temperature,
            LLMCapability.Tools,
        ),
    )
}