# Konvo Improvement Plan

This document translates the requirements into a concrete, staged plan with rationale, scope, and acceptance criteria. It is organized by themes and system areas and tailored to the current Konvo modules and practices.

Date: 2025-08-10


## Summary of Key Goals and Constraints

- Conversation data must be stored in a database, with three pluggable storage options: in-memory, file system (Kotlinx Serialization + Kotlinx IO), and database (SqlDelight).
- Application settings must be persisted via two storage options: file system (Kotlinx Serialization + Kotlinx IO) and a persistent store on mobile platforms.
- Character providers must support reading character cards embedded in PNG metadata using the `kim` library and follow the Character Cards v3 specification.
- Compose frontend must add: a Settings screen, a Conversation List side panel, and a visual Character Selector showing avatar and name.
- Multiplatform constraints: Keep core logic in commonMain where possible; use expect/actual implementations for platform-specific details.


## Architectural Principles and Cross-Cutting Decisions

- Storage Abstraction: Define clear repository/service interfaces in `konvo-core` that hide specific storage backends. This enables swapping between in-memory, file, and database without touching UI/frontends.
- Dependency Injection (Manual): Pass specific repository implementations via constructors to frontends (Discord, Compose) consistent with project guidelines.
- Serialization Format: Use Kotlinx Serialization with versioned schemas for both conversations (file backend) and settings to enable future migrations.
- Database Portability: Use SqlDelight with common schema definitions and platform-specific drivers (JVM/Android/iOS as applicable).
- Error Handling: Use coroutine exception handlers; repositories should surface domain-safe errors.
- Testing: Place unit tests in `src/commonTest`. Create integration tests for repository backends on supported targets.


## Core: Conversation Storage

### 1. Define Core Domain and Repository Interfaces (konvo-core)
- Changes:
  - Introduce data models for Conversation, Transcript, Message, Participant (if not already present) ensuring they’re in `commonMain`.
  - Define `ConversationRepository` interface in `commonMain` with operations:
    - `createConversation`, `getConversation(id)`, `listConversations`, `appendMessage(conversationId, message)`, `updateConversation`, `deleteConversation`, `deleteAll`.
  - Define selection/sorting parameters for listing (e.g., recent first) to support UI side panel needs.
- Rationale:
  - Decouples storage mechanism from business logic and UIs; enables plug-in backends and easy testing.
- Acceptance Criteria:
  - Interfaces compile in `konvo-core` and can be depended upon by frontends.

### 2. In-Memory Implementation (konvo-core)
- Changes:
  - Implement `InMemoryConversationRepository` with thread-safe collections (e.g., kotlinx-atomicfu's `atomic()` + maps) in `commonMain`.
- Rationale:
  - Fast baseline for testing and samples; zero configuration.
- Acceptance Criteria:
  - Unit tests proving CRUD and ordering semantics.

### 3. File System Implementation with Kotlinx Serialization + Kotlinx IO (konvo-core)
- Changes:
  - Add serializable data models for Conversation, Transcript, Message, Participant in `commonMain`, that mirror the domain models.
    - Use the IDs of entities for cross-entity references, instead of the pointers used in the domain models.
  - Implement `FileConversationRepository` in `commonMain` using Kotlinx Serialization and Kotlinx IO.
  - Store conversations as NDJSON or per-conversation JSON files under a configurable data directory.
  - Include basic compaction and index for listing (e.g., metadata file for fast reads).
- Rationale:
  - Provides portable storage without database setup; aligns with requirements and existing sample app data dirs.
- Acceptance Criteria:
  - Read/write round-trip tests; directory configurable via configuration; resilience to partial files.

### 4. Database Implementation with SqlDelight (konvo-core)
- Changes:
  - Add SqlDelight schema for conversations, messages, participants.
  - Implement `SqlDelightConversationRepository` with paging/listing support and transactions for append/update.
  - Provide driver wiring for JVM target initially; leave stubs or documentation for mobile drivers.
- Rationale:
  - Meets requirement that conversations must be stored in a database; supports scale and indexing.
- Acceptance Criteria:
  - Integration tests on JVM running against SqlDelight driver; migrations strategy documented.

### 5. Configuration and Selection of Backend
- Changes:
  - Extend `konvo.json5` configuration schema to allow selecting conversation storage backend and configuring its parameters (e.g., path, DB file).
  - Wire configuration into samples (Discord bot, Compose app) so they pick the right repository at startup.
- Rationale:
  - Makes backend choice explicit and user-configurable; eases testing different options.
- Acceptance Criteria:
  - Changing config switches the concrete repository with no code changes to frontends.


## Core: Application Settings

### 6. Define Settings Model and Repository Interface (konvo-core)
- Changes:
  - Create `AppSettings` data class covering existing configurable options (data directory, model providers, MCP tools, Discord token via secure path/reference), marked `@Serializable`.
  - Define `SettingsRepository` interface with get/set/update semantics and change flow (e.g., `StateFlow<AppSettings>`).
- Rationale:
  - Centralizes settings management and allows UIs to observe changes.
- Acceptance Criteria:
  - Compiles in `commonMain`; unit tests validate load-save-update.

### 7. File System Settings Repository (Kotlinx Serialization + IO)
- Changes:
  - Implement `FileSettingsRepository` persisting `AppSettings` to JSON5.
  - Provide migration/version field inside settings.
- Rationale:
  - Fulfills file-based storage option; aligns with existing configuration files used by samples.
- Acceptance Criteria:
  - Unit tests; safe concurrent writes; rollback on errors.

### 8. Mobile Persistent Store Settings Repository
- Changes:
  - Define `expect/actual` storage API for mobile (e.g., Android DataStore/SharedPreferences; iOS NSUserDefaults/Files), implemented in mobile targets when added.
  - For now, provide JVM/desktop stub and a clean interface to plug in mobile implementations later.
- Rationale:
  - Satisfies requirement and keeps architecture ready for mobile targets.
- Acceptance Criteria:
  - Actual implementations available for mobile targets when enabled; documented fallback on JVM.


## Core: Character Providers (PNG Metadata, Character Cards v3)

### 9. Character Card Reader Abstraction
- Changes:
  - Define `CharacterProvider` interface capable of listing and loading characters from a directory and from images.
  - Keep or extend existing `FileSystemCharacterProvider` to support PNG embedded metadata.
- Rationale:
  - Encapsulates character loading logic and allows Compose/Discord UIs to pull avatars and names easily.
- Acceptance Criteria:
  - Interface usable from UIs; unit tests on parsing behavior.

### 10. Implement PNG Metadata Extraction with `kim`
- Changes:
  - Use the `kim` library to read PNG metadata chunks and extract embedded Character Cards v3 JSON.
  - Validate against the v3 spec fields; provide tolerant parsing with warnings for unknown fields.
  - Map to internal `CharacterCard` domain model (already present in `konvo-core`), including avatar URL/path.
- Rationale:
  - Meets requirement to handle embedded cards; leverages a maintained library.
- Acceptance Criteria:
  - Tests with sample PNGs containing embedded cards; graceful failure for images without metadata.

### 11. Fallback: Sidecar JSON Files
- Changes:
  - If PNG metadata is absent, look for adjacent `*.card.json` (v3 schema) to maximize compatibility.
- Rationale:
  - Improves robustness and developer experience while still prioritizing embedded metadata.
- Acceptance Criteria:
  - Unit tests verifying fallback behavior.


## Compose Frontend Enhancements

### 12. Settings Screen
- Changes:
  - Create a new Compose screen (e.g., `SettingsScreen`) in `konvo-frontend-compose` using Material 3.
  - Bind to `SettingsRepository` via a ViewModel; allow editing data directory, model provider selections, and other safe settings.
  - Provide validation and save/apply actions; reflect current values via `StateFlow`.
- Rationale:
  - Provides user control as required; reuses core settings abstractions.
- Acceptance Criteria:
  - Navigable screen; settings persist and rehydrate on restart.

### 13. Conversation List Side Panel
- Changes:
  - Add a persistent side panel (NavigationRail/Drawer) listing recent conversations with timestamps and snippet.
  - Use `ConversationRepository.listConversations` with sorting/paging; support selection and delete.
  - Ensure responsive layout across desktop window sizes.
- Rationale:
  - Improves navigation and aligns with requirement.
- Acceptance Criteria:
  - List reflects real repository data; selecting loads conversation into main view.

### 14. Visual Character Selector (Avatar + Name)
- Changes:
  - Implement a grid/list showing character avatars and names; supports search/filter.
  - Leverage `CharacterProvider` to list characters and get avatar images.
  - Add loading/error states and fallback avatar when missing.
- Rationale:
  - Enhances onboarding for new conversations and character choice.
- Acceptance Criteria:
  - Selector displays avatars and names; selection triggers new conversation setup.


## Discord Frontend (Minimal Touch)

### 15. Wire Repositories and Character Provider
- Changes:
  - Pass repository implementations via constructors per configuration (in-memory/file/SqlDelight).
  - Use `CharacterProvider` to resolve character details for messages.
- Rationale:
  - Keeps parity with Compose app and validates abstractions in multiple frontends.
- Acceptance Criteria:
  - Bot runs with chosen backend; commands continue to work.


## Configuration and Bootstrapping

### 16. Extend konvo.json5 Schema and Loading
- Changes:
  - Add fields:
    - `storage.conversations`: `inMemory` | `file` | `sqlDelight` with per-backend configs
    - `storage.settings`: `file` | `mobile`
    - `paths.dataDirectory`, `database.filePath` (for desktop/JVM)
  - Update sample configs in `samples/*/config/konvo.sample.json5`.
- Rationale:
  - Centralizes and documents runtime selection of backends.
- Acceptance Criteria:
  - Apps start using configured backends; misconfigurations produce clear errors.


## Testing Strategy

### 17. Unit Tests (commonTest)
- ConversationRepository contract tests applied to all implementations via shared test suite.
- SettingsRepository tests for load/save/update.
- Character parsing tests for PNG embedded metadata and sidecar fallback.

### 18. Integration Tests (JVM)
- SqlDelight repository tests against a temporary DB file.
- File repository tests against temporary directories.

### 19. UI Tests (Compose, optional but recommended)
- Simple state-driven tests validating that lists render expected items given mocks.


## Performance, Resilience, and Observability

### 20. Performance
- Use streaming/NDJSON or chunked writes for large conversations in file backend.
- Use indices in SqlDelight schema for conversation list ordering.

### 21. Resilience
- Safe-write pattern (write temp + atomic rename) for file settings and conversations.
- Handle I/O errors gracefully with user-visible messages in UI.

### 22. Observability
- Add logging (Kotlin Logging) around repository operations; feature flags for verbose logs in samples.


## Migration and Compatibility

### 23. Versioning
- Include `schemaVersion` in serialized conversations and settings for future migrations.

### 24. Backward Compatibility
- Tolerant parsing for Character Cards; ignore unknown fields per v3 spec.


## Documentation Updates

### 25. README and Docs
- Add short instructions to README for selecting storage backends.
- Document character card support and how to add embedded metadata.
- Update sample app READMEs to point to new settings screen and configuration options.


## Milestones and Timeline

1. M1: Core interfaces + In-memory repository + Settings interface/file repository + Character provider abstraction (1–2 weeks).
2. M2: File-based conversation repository + PNG metadata via `kim` + sidecar fallback + unit tests (1–2 weeks).
3. M3: SqlDelight schema and implementation + JVM integration tests (1–2 weeks).
4. M4: Compose UI screens (Settings, Conversation List, Character Selector) with wiring to repositories (1–2 weeks).
5. M5: Polishing, docs, and cross-frontend wiring (Discord) (1 week).


## Risks and Mitigations

- SqlDelight driver differences: Start with JVM; document mobile drivers for future targets. Mitigation: isolated driver wiring.
- Data migrations: Schema evolution for conversations. Mitigation: version fields + SqlDelight migrations + upcasters for file JSON.


## Acceptance Criteria Checklist

- Conversation storage:
  - In-memory, file-based, and SqlDelight backends implemented and selectable at runtime.
  - CRUD, listing, and append behavior covered by tests.
- Settings:
  - File-based and mobile persistent (design + JVM stub) repositories available; observable Settings state.
- Character providers:
  - PNG metadata extraction via `kim` compliant with v3 spec; sidecar JSON fallback.
- Compose frontend:
  - Settings screen, Conversation List side panel, and Character Selector implemented and wired.
- Documentation and samples updated to reflect configuration and usage.
