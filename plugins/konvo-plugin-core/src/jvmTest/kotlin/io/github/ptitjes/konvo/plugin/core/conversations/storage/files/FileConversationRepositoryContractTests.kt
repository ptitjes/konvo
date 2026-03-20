package io.github.ptitjes.konvo.plugin.core.conversations.storage.files

import io.github.ptitjes.konvo.plugin.core.conversations.*
import io.github.ptitjes.konvo.plugin.core.conversations.storage.*
import io.github.ptitjes.konvo.plugin.core.platform.*
import io.github.ptitjes.konvo.plugin.core.util.*
import io.github.ptitjes.syrup.specification.*
import kotlinx.io.files.Path
import org.kodein.di.*
import kotlin.io.path.createTempDirectory
import kotlin.reflect.*

class FileConversationRepositoryContractTests : ConversationRepositoryContractTests() {

    override fun createRepository(timeProvider: TimeProvider): ConversationRepository {
        val tmp = createTempDirectory("konvo-file-repo-")
        val root = Path(tmp.toString())

        val storagePaths = object : StoragePaths {
            override val configDirectory: Path = Path(root, "config")
            override val dataDirectory: Path = Path(root, "data")
            override val cacheDirectory: Path = Path(root, "cache")
        }

        val context = object : MockPluginContext() {
            override fun <T : Any> contributions(extensionPoint: ExtensionPoint.Plural<T>): LazyDelegate<Set<T>> =
                object : LazyDelegate<Set<T>> {
                    override fun provideDelegate(receiver: Any?, prop: KProperty<Any?>): Lazy<Set<T>> =
                        lazy { emptySet() }
                }
        }

        val actionRegistry = DefaultActionRegistry(context)
        val interactionProtocolRegistry = DefaultInteractionProtocolRegistry(context)

        return FileConversationRepository(
            storagePaths = storagePaths,
            actionRegistry = actionRegistry,
            interactionProtocolRegistry = interactionProtocolRegistry,
            pluginContext = context
        )
    }
}
