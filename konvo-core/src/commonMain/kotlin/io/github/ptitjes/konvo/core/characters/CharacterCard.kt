package io.github.ptitjes.konvo.core.characters

interface CharacterCard {
    val name: String
    val avatarUrl: String?
    val description: String
    val personality: String
    val scenario: String
    val messageExample: String
    val greetings: List<String>
    val tags: List<String>
}
