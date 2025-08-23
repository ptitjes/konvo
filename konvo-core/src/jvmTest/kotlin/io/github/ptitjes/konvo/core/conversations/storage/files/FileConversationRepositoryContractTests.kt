package io.github.ptitjes.konvo.core.conversations.storage.files

import io.github.ptitjes.konvo.core.conversations.storage.*
import io.github.ptitjes.konvo.core.util.*
import kotlinx.io.files.Path
import kotlin.io.path.createTempDirectory

class FileConversationRepositoryContractTests : ConversationRepositoryContractTests() {

    override fun createRepository(timeProvider: TimeProvider): ConversationRepository {
        val tmp = createTempDirectory("konvo-file-repo-")
        val root = Path(tmp.toString())
        return FileConversationRepository(root, timeProvider = timeProvider)
    }
}
