package io.github.ptitjes.konvo.core.conversations.storage.inmemory

import io.github.ptitjes.konvo.core.conversations.storage.*

class InMemoryConversationRepositoryContractTests : ConversationRepositoryContractTests() {

    override fun createRepository(): ConversationRepository = InMemoryConversationRepository()
}
