package io.github.ptitjes.konvo.core.conversations.storage.files

import io.github.ptitjes.konvo.core.conversations.storage.*
import kotlinx.io.files.Path
import kotlin.io.path.createTempDirectory

class FileConversationRepositoryContractTests : ConversationRepositoryContractTests() {

    override fun createRepository(): ConversationRepository {
        val tmp = createTempDirectory("konvo-file-repo-")
        val root = Path(tmp.toString())
        return FileConversationRepository(root)
    }
}
