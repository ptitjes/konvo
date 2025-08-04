# Konvo Project Development Guidelines

This document provides essential information for developers working on the Konvo project.

## Build and Configuration Instructions

### Project Structure

Konvo is a Kotlin Multiplatform project with JVM as the primary target. The project consists of several modules:

- `konvo-core`: Core functionality for conversations, AI integration, and tools
- `konvo-frontend-discord`: Discord bot frontend implementation
- `konvo-frontend-compose`: Compose UI frontend implementation
- `konvo-mcp-prompt-collection`: MCP (Model Context Protocol) prompt collection
- `konvo-mcp-web-tools`: Web tools for MCP
- `samples`: Sample applications
  - `samples/discord-bot`: Discord bot sample application
  - `samples/standalone-compose-app`: Standalone Compose UI application

### Build Setup

1. The project uses Gradle with Kotlin DSL for build configuration
2. JVM toolchain 17 is required
3. Build the project with:

```bash
./gradlew build
```

### Configuration

Each sample application has its own configuration:

1. For each sample application:
   - Create a `data` directory with a `characters` subdirectory for character cards
   - Create a `config/konvo.json5` file using `config/konvo.sample.json5` as a template
   - Configure the following in your JSON5 file:
     - `dataDirectory`: Path to the data directory
     - `modelProviders`: Configuration for AI model providers (e.g., Ollama)
     - `mcp`: Model Context Protocol configuration
     - For Discord bot, also configure `discord.token` with your Discord bot token

2. Start the Discord bot application with:

```bash
./gradlew :samples:discord-bot:run
```

3. Start the Compose UI application with:

```bash
./gradlew :samples:standalone-compose-app:run
```

## Testing Information

### Test Structure

1. Tests are written using the Kotlin Test framework (`kotlin.test`)
2. Tests are located in the `src/commonTest` directory of each module
3. Test classes are named with a `Tests` suffix (e.g., `MarkdownContentBuilderTests`)
4. Test methods are named in plain English with spaces enclosed in backticks and use the `@Test` annotation

### Running Tests

Run tests for all modules:

```bash
./gradlew test
```

Run tests for a specific module:

```bash
./gradlew :konvo-core:test
```

Run a specific test class:

```bash
./gradlew :konvo-core:test --tests "io.github.ptitjes.konvo.core.ai.koog.CallFixingPromptExecutorTests"
```

### Writing New Tests

1. Create a new test class in the appropriate module's `src/commonTest/kotlin` directory
2. Import the Kotlin test framework: `import kotlin.test.*`
3. Use the `@Test` annotation for test methods
4. Use assertion methods like `assertEquals`, `assertTrue`, etc.

## Code Style and Development Practices

### Kotlin Idioms

The project follows standard Kotlin idioms and best practices:

1. Use of sealed interfaces/classes for type-safe enumerations
2. Data classes for value objects
3. Extension functions for enhancing existing classes
4. Coroutines for asynchronous programming
5. Functional programming approaches where appropriate

### Project-Specific Patterns

1. **Conversation Architecture**: The project uses a channel-based approach for communication between user and assistant events
2. **Dependency Injection**: Manual dependency injection through constructors
3. **Error Handling**: Coroutine exception handlers for graceful error recovery
4. **Multiplatform**: Code is structured to support Kotlin Multiplatform with common code in `commonMain`

### Logging

The project uses Kotlin Logging:

```kotlin
private val logger = KotlinLogging.logger {}
logger.info { "Log message" }
logger.error(exception) { "Error message" }
```

### Discord Integration

The project uses the Kord library for Discord integration:

1. Commands are registered using the Discord slash command system
2. Interactions are handled through event listeners
3. Messages use Discord's component system for rich UI

## Additional Development Information

### Debugging

1. Enable more verbose logging by configuring the logger level in the sample application's `src/main/resources/simplelogger.properties`
2. Use the `--debug` flag with Gradle for build debugging: `./gradlew --debug :samples:discord-bot:run` or `./gradlew --debug :samples:standalone-compose-app:run`

### Working with AI Models

1. The project supports multiple AI model providers through the `modelProviders` configuration
2. Ollama is the default local model provider
3. Model Context Protocol (MCP) is used for tool integration

### Character Cards

Character cards are JSON files in the `data/characters` directory with the following structure:
- Character name
- Avatar URL
- Greeting messages
- Other character-specific information

### Adding New Features

1. Core functionality should be added to the `konvo-core` module
2. Discord-specific features should be added to the `konvo-frontend-discord` module
3. Compose UI-specific features should be added to the `konvo-frontend-compose` module
4. New MCP tools should be added to the appropriate MCP module
