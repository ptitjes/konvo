# Requirements

## Core

### Conversation Storage

- Conversations must be stored in a database
- Provide three different storage options:
  - In-memory
  - File system (using Kotlinx Serialization and Kotlinx IO)
  - Database (using SqlDelight)

### Application Settings

- Provide a way to store the application settings
- Provide two different storage options:
  - File system (using Kotlinx Serialization and Kotlinx IO)
  - Persistent store (on mobile platforms)

### Character Providers

- Provide a way to extract the metadata from a character card:
  - Use [kim](https://github.com/Ashampoo/kim) to extract the metadata
  - Character cards are embedded in PNG metadata as a `tEXt` chunk
  - Follow the Character Cards v3 [specification](https://github.com/kwaroran/character-card-spec-v3/blob/main/SPEC_V3.md)
- Provide a way to load character cards from:
    - Local files
    - Remote URLs

## Compose Frontend

- Add a settings screen
- Add a Conversation List side panel
- Add a visual Character Selector that displays the characters' avatar and name

# Dependencies

- [Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)
  - Supports all platforms: JVM, Android, iOS, JS, WASM, Windows, Linux, macOS
  - Supports JSON5, via special configuration of the `Json` instance:
    ```kotlin
        @OptIn(ExperimentalSerializationApi::class)
        private val configurationJson = Json {
            isLenient = true
            allowComments = true
            allowTrailingComma = true
        }
    ```
- [Ktor](https://ktor.io/)
- [Kodein](https://github.com/kosi-libs/Kodein)
    - Supports all platforms: JVM, Android, iOS, JS, WASM, Windows, Linux, macOS
- [SqlDelight](https://github.com/cashapp/sqldelight)
    - Supports all platforms: JVM, Android, iOS, JS, WASM, Windows, Linux, macOS
- [Kim](https://github.com/Ashampoo/kim)
  - Supports all platforms: JVM, Android, iOS, JS, WASM, Windows, Linux, macOS
