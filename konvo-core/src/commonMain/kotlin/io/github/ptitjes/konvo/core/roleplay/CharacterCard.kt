package io.github.ptitjes.konvo.core.roleplay

interface CharacterCard {
    val id: String
    val name: String
    val avatarUrl: String?
    val description: String
    val personality: String
    val scenario: String
    val dialogueExamples: String
    val systemPrompt: String?
    val postHistoryInstructions: String?
    val greetings: List<String>
    val tags: List<String>
    val characterBook: Lorebook?
}
