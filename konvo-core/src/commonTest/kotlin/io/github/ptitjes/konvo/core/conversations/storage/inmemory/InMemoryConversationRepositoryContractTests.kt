package io.github.ptitjes.konvo.core.conversations.storage.inmemory

import io.github.ptitjes.konvo.core.conversations.storage.*
import io.github.ptitjes.konvo.core.util.*

class InMemoryConversationRepositoryContractTests : ConversationRepositoryContractTests() {

    override fun createRepository(timeProvider: TimeProvider): ConversationRepository =
        InMemoryConversationRepository(timeProvider = timeProvider)
}
