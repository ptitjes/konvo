package io.github.ptitjes.konvo.core.settings

import io.github.ptitjes.konvo.core.platform.*
import kotlinx.coroutines.flow.*
import kotlinx.io.*
import kotlinx.io.files.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

/**
 * File system implementation of [SettingsRepository].
 *
 * - Uses [io.github.ptitjes.konvo.core.platform.StoragePaths.configDirectory] as the base directory.
 * - Each settings section is stored in its own `${name}.json5` file.
 * - JSON is configured to be pretty and lenient, and ignore unknown keys.
 */
class FileSystemSettingsRepository(
    private val storagePaths: StoragePaths,
    private val fileSystem: FileSystem = defaultFileSystem,
) : SettingsRepository {

    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        private val json: Json = Json {
            prettyPrint = true
            isLenient = true
            allowComments = true
            allowTrailingComma = true
            ignoreUnknownKeys = true
        }
    }

    private val baseDir: Path get() = storagePaths.configDirectory

    private fun fileFor(name: String): Path = Path(baseDir, "$name.json5")

    // Keep a StateFlow per settings section to provide a Flow API and push updates
    private val flows: MutableMap<String, MutableStateFlow<Any?>> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    override fun <T> getSettings(key: SettingsSectionKey<T>): Flow<T> {
        val state = flows.getOrPut(key.name) {
            val initial: Any? = try {
                readFromDisk(key)
            } catch (_: Throwable) {
                key.defaultValue
            }
            MutableStateFlow(initial)
        }
        return (state.asStateFlow() as StateFlow<T>)
    }

    override suspend fun <T> updateSettings(key: SettingsSectionKey<T>, value: T) {
        writeToDisk(key, value)
        @Suppress("UNCHECKED_CAST")
        val state = flows[key.name] as? MutableStateFlow<T>
        if (state == null) {
            // Create the flow if it didn't exist yet so future subscribers see the latest value
            @Suppress("UNCHECKED_CAST")
            flows[key.name] = MutableStateFlow(value as Any?)
        } else {
            state.value = value
        }
    }

    private fun <T> readFromDisk(key: SettingsSectionKey<T>): T {
        val path = fileFor(key.name)
        return try {
            if (!fileSystem.exists(path)) return key.defaultValue
            fileSystem.source(path).buffered().use { src ->
                val content = src.readString()
                json.decodeFromString(key.serializer, content)
            }
        } catch (_: Throwable) {
            // On any error, return the default value to keep the app functioning.
            key.defaultValue
        }
    }

    private fun <T> writeToDisk(key: SettingsSectionKey<T>, value: T) {
        val path = fileFor(key.name)
        atomicWrite(path) { sink ->
            val content = json.encodeToString(key.serializer, value)
            sink.writeString(content)
        }
    }

    /** Write to a temp file in the same directory and then atomically move into place. */
    private fun atomicWrite(destination: Path, writer: (Sink) -> Unit) {
        val parent = destination.parent ?: error("Target must have a parent directory: $destination")
        // Ensure directory exists
        fileSystem.createDirectories(parent)

        val tmp = Path(parent, destination.name + ".tmp")
        fileSystem.sink(tmp).buffered().use { writer(it) }
        fileSystem.atomicMove(tmp, destination)
    }
}
