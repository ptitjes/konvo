package io.github.ptitjes.konvo.core.conversation.storage.inmemory

import io.github.ptitjes.konvo.core.conversation.storage.*

class InMemoryConversationRepositoryContractTests : ConversationRepositoryContractTests() {

    override fun createRepository(): ConversationRepository = InMemoryConversationRepository()
}
