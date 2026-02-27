# Conversation Model Implementation Plan

This document provides a detailed, step-by-step implementation plan for the conversation model refactoring described in `conversation-model-todo.md`.

## Overview

The implementation is divided into four main phases:
1. **Initial Refactorings** - Rename existing types and methods
2. **Interaction Type Introduction** - Add new sealed class hierarchy
3. **ConversationTranscript Introduction** - Replace event lists with transcript abstraction
4. **Interaction Property in Action** - Wire up interactions to actions

---

## Phase 1: Initial Refactorings

### 1.1 Rename `Event` to `Action`

#### Core Model Changes

- [ ] **Rename `Event` class to `Action`**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/Event.kt`
  - Rename class `Event` → `Action`
  - Update nested interfaces: `Event.Payload` → `Action.Payload`, `Event.Agent` → `Action.Agent`, `Event.User` → `Action.User`
  - Rename file to `Action.kt`

- [ ] **Update all event payload interfaces to extend `Action.Payload`**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/events/Messaging.kt`
  - Change `sealed interface Messaging : Event.Payload` → `sealed interface Messaging : Action.Payload`
  - Update implementations: `Event.Agent` → `Action.Agent`, `Event.User` → `Action.User`

- [ ] **Update remaining event payload files**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/events/Presence.kt`
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/events/AgentCapabilities.kt`
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/events/AgentProcessing.kt`
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/events/ToolUsage.kt`
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/events/ConversationControl.kt`
  - For each: Update `Event.Payload` → `Action.Payload`, `Event.Agent` → `Action.Agent`, `Event.User` → `Action.User`

- [ ] **Update serialization module**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/events/CoreEvents.kt`
  - Rename `CoreEvents` serialization module to handle `Action.Payload` instead of `Event.Payload`
  - Consider renaming to `CoreActions` for consistency

#### Repository and Storage Layer

- [ ] **Update DTO classes**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/Dto.kt`
  - Rename `EventDto` → `ActionDto`
  - Update field: `val payload: Event.Payload` → `val payload: Action.Payload`
  - Update mapper methods: `toDto(e: Event<*>)` → `toDto(a: Action<*>)`, `fromDto(e: EventDto)` → `fromDto(a: ActionDto)`

- [ ] **Update ConversationRepository interface**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/ConversationRepository.kt`
  - Rename method: `getEvents(conversationId: String): Flow<List<Event<*>>>` → `getActions(conversationId: String): Flow<List<Action<*>>>`
  - Rename method: `appendEvent(conversationId: String, event: Event<*>)` → `appendAction(conversationId: String, action: Action<*>)`
  - Update documentation comments

- [ ] **Update FileConversationRepository implementation**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/FileConversationRepository.kt`
  - Update method implementations: `getEvents` → `getActions`, `appendEvent` → `appendAction`
  - Update `readEvents` → `readActions` helper method
  - Update JSON serialization calls to use `ActionDto`
  - Note: File name `events.ndjson` can remain for now (or rename to `actions.ndjson`)

- [ ] **Update InMemoryConversationRepository implementation**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/inmemory/InMemoryConversationRepository.kt`
  - Update field: `private val events` → `private val actions`
  - Update state flow: `eventsState` → `actionsState`
  - Update method implementations: `getEvents` → `getActions`, `appendEvent` → `appendAction`

#### Core Conversation Layer

- [ ] **Update Conversation class**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/Conversation.kt`
  - Update `ConversationState.Loaded`: `transcript: List<Event<*>>` → `transcript: List<Action<*>>`
  - Update field: `private val _events` → `private val _actions`
  - Update calls: `repository.getEvents` → `repository.getActions`, `repository.appendEvent` → `repository.appendAction`
  - Update `restoreAgents` method parameter: `List<Event<*>>` → `List<Action<*>>`

- [ ] **Update ConversationAgentView interface**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/ConversationAgentView.kt`
  - Update field: `val events: SharedFlow<Event<*>>` → `val actions: SharedFlow<Action<*>>`
  - Update method: `suspend fun send(payload: Event.Agent)` → `suspend fun send(payload: Action.Agent)`

- [ ] **Update ConversationUserView interface**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/ConversationUserView.kt`
  - Update field: `val events: SharedFlow<Event<*>>` → `val actions: SharedFlow<Action<*>>`
  - Update method: `suspend fun send(payload: Event.User)` → `suspend fun send(payload: Action.User)`

- [ ] **Update Agent interface**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/Agent.kt`
  - Update method: `suspend fun restoreSession(transcript: List<Event<*>>, ...)` → `suspend fun restoreSession(transcript: List<Action<*>>, ...)`

- [ ] **Update Transcript class**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/Transcript.kt`
  - Update field: `private val _events` → `private val _actions`
  - Update property: `val events` → `val actions`
  - Update method: `append(event: Event<*>)` → `append(action: Action<*>)`
  - Update return types: `List<Event<*>>` → `List<Action<*>>`

#### Agent Implementation

- [ ] **Update DefaultAgent**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/DefaultAgent.kt`
  - Update all references from `Event` → `Action`
  - Update method implementations and internal logic

- [ ] **Update agent toolkit files**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/KoogEventOps.kt`
  - Rename file to `KoogActionOps.kt`
  - Update all references from `Event` → `Action`

- [ ] **Update other agent implementations**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/RoleplayAgent.kt`
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/QuestionAnswerAgent.kt`
  - Update all references from `Event` → `Action`

#### Test Files

- [ ] **Update test files in konvo-core**
  - File: `konvo-core/src/jvmTest/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/FileConversationRepositoryContractTests.kt`
  - File: `konvo-core/src/jvmTest/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/FileConversationRepositoryPartialFilesTests.kt`
  - File: `konvo-core/src/commonTest/kotlin/io/github/ptitjes/konvo/core/conversations/storage/ConversationRepositoryContractTests.kt`
  - Update all references from `Event` → `Action`, `events` → `actions`

#### Frontend Module (Compose)

- [ ] **Update ConversationViewModel**
  - File: `konvo-frontend-compose/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/compose/conversations/ConversationViewModel.kt`
  - Update all references from `Event` → `Action`

- [ ] **Update conversation view components**
  - File: `konvo-frontend-compose/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/compose/conversations/ConversationPane.kt`
  - Update all event references to action references

- [ ] **Update view state files**
  - File: `konvo-frontend-compose/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/compose/conversations/views/CoreItemPanels.kt`
  - File: `konvo-frontend-compose/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/compose/conversations/views/PresenceViewState.kt`
  - Update all references from `Event` → `Action`

- [ ] **Update ConversationViewStates**
  - File: `konvo-frontend-compose/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/compose/conversations/spi/ConversationViewStates.kt`
  - Update all references from `Event` → `Action`

#### Frontend Module (Discord)

- [ ] **Update DiscordBot**
  - File: `konvo-frontend-discord/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/discord/DiscordBot.kt`
  - Update `handleAssistantEvents` → `handleAssistantActions`
  - Update all references from `Event` → `Action`

---

### 1.2 Rename `ConversationAgentView` to `InteractionDevice.Agent`

- [ ] **Create new InteractionDevice interface**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/InteractionDevice.kt` (new file)
  - Create sealed interface `InteractionDevice` with common properties:
    - `val participant: Participant`
    - `val actions: SharedFlow<Action<*>>`
  - Create nested interface `InteractionDevice.Agent` extending `InteractionDevice`
    - Override: `val participant: Participant.Agent`
    - Method: `suspend fun send(payload: Action.Agent)`
  - Create nested interface `InteractionDevice.User` extending `InteractionDevice`
    - Override: `val participant: Participant.User`
    - Method: `suspend fun send(payload: Action.User)`

- [ ] **Migrate ConversationAgentView to InteractionDevice.Agent**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/ConversationAgentView.kt`
  - Mark as `@Deprecated` with replacement message
  - Make it extend `InteractionDevice.Agent`
  - Keep file temporarily for migration

- [ ] **Update Agent interface**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/Agent.kt`
  - Change parameter type: `ConversationAgentView` → `InteractionDevice.Agent`

- [ ] **Update DefaultAgent**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/DefaultAgent.kt`
  - Update all parameter types and field types: `ConversationAgentView` → `InteractionDevice.Agent`

- [ ] **Update ConversationFeature and related files**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/ConversationFeature.kt`
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/ConversationFeatureConfig.kt`
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/Nodes.kt`
  - Update all references: `ConversationAgentView` → `InteractionDevice.Agent`

- [ ] **Update Conversation class implementation**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/Conversation.kt`
  - Update inner class `AgentViewImpl` to implement `InteractionDevice.Agent`

- [ ] **Remove deprecated ConversationAgentView**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/ConversationAgentView.kt`
  - Delete file once all references are updated

---

### 1.3 Rename `ConversationUserView` to `InteractionDevice.User`

- [ ] **Migrate ConversationUserView to InteractionDevice.User**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/ConversationUserView.kt`
  - Mark as `@Deprecated` with replacement message
  - Make it extend `InteractionDevice.User`
  - Keep `state: StateFlow<ConversationState>` property for now (will be moved later)

- [ ] **Update Conversation class implementation**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/Conversation.kt`
  - Update `newUserView()` return type: `ConversationUserView` → `InteractionDevice.User`
  - Update inner class `UserViewImpl` to implement `InteractionDevice.User`

- [ ] **Update frontend compose module**
  - Files: All files in `konvo-frontend-compose/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/compose/conversations/`
  - Update context receivers and parameter types: `ConversationUserView` → `InteractionDevice.User`
  - Key files:
    - `ConversationPane.kt`
    - `EditableConversationTitle.kt`
    - `ConversationViewModel.kt`
    - `views/CoreItemPanels.kt`

- [ ] **Update frontend discord module**
  - File: `konvo-frontend-discord/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/discord/DiscordBot.kt`
  - Update all references: `ConversationUserView` → `InteractionDevice.User`

- [ ] **Remove deprecated ConversationUserView**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/ConversationUserView.kt`
  - Delete file once all references are updated

---

### 1.4 Rename `send(...)` methods to `act(...)`

#### Core Module

- [ ] **Update InteractionDevice interfaces**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/InteractionDevice.kt`
  - Rename: `suspend fun send(payload: Action.Agent)` → `suspend fun act(payload: Action.Agent)`
  - Rename: `suspend fun send(payload: Action.User)` → `suspend fun act(payload: Action.User)`

- [ ] **Update Conversation class implementations**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/Conversation.kt`
  - Update `AgentViewImpl.send` → `AgentViewImpl.act`
  - Update `UserViewImpl.send` → `UserViewImpl.act`
  - Update call in `join()` method: `newUserView().send(...)` → `newUserView().act(...)`

- [ ] **Update DefaultAgent implementation**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/DefaultAgent.kt`
  - Update all calls: `conversation.send(...)` → `conversation.act(...)`

- [ ] **Update agent toolkit files**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/Nodes.kt`
  - Update all calls: `.send(...)` → `.act(...)`

- [ ] **Update other agent implementations**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/RoleplayAgent.kt`
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/QuestionAnswerAgent.kt`
  - Update all calls: `.send(...)` → `.act(...)`

#### Frontend Compose Module

- [ ] **Update ConversationPane**
  - File: `konvo-frontend-compose/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/compose/conversations/ConversationPane.kt`
  - Update extension function: `suspend fun InteractionDevice.User.sendMessage(...)` → `suspend fun InteractionDevice.User.actMessage(...)`
  - Update all calls within the function

- [ ] **Update CoreItemPanels**
  - File: `konvo-frontend-compose/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/compose/conversations/views/CoreItemPanels.kt`
  - Update extension function: `suspend fun InteractionDevice.User.sendToolUseApproval(...)` → `suspend fun InteractionDevice.User.actToolUseApproval(...)`
  - Update all internal calls

- [ ] **Search and update remaining frontend compose files**
  - Use IDE search for `.send(` in `konvo-frontend-compose/` directory
  - Update all user action sending calls

#### Frontend Discord Module

- [ ] **Update DiscordBot**
  - File: `konvo-frontend-discord/src/commonMain/kotlin/io/github/ptitjes/konvo/frontend/discord/DiscordBot.kt`
  - Update all calls: `conversation.send(...)` → `conversation.act(...)`

---

### 1.5 Add `startInteraction` and `endInteraction` methods

- [ ] **Add methods to InteractionDevice.Agent**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/InteractionDevice.kt`
  - Add method signature: `suspend fun startInteraction(protocol: InteractionProtocol, parent: Interaction? = null, trigger: Action<*>? = null): Interaction`
  - Add method signature: `suspend fun endInteraction(interaction: Interaction)`

- [ ] **Note: Implementation will be completed in Phase 2** after `Interaction` and `InteractionProtocol` classes are introduced

---

## Phase 2: Interaction Type Introduction

### 2.1 Introduce `ConversationEntry` sealed class hierarchy

- [x] **Create ConversationEntry sealed class**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/ConversationEntry.kt` (new file)
  - Create `sealed class ConversationEntry` with internal constructor
  - Properties:
    - `val timestamp: Instant`
    - `val sender: Participant`

- [x] **Make `Action` extend `ConversationEntry`**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/Action.kt`
  - Change from `data class Action<out T : Action.Payload>(...)` to extend `ConversationEntry`
  - Constructor should call `super(timestamp, sender)` with ConversationEntry constructor
  - Keep existing properties: `id`, `recipients`, `payload`
  - Note: `timestamp` and `sender` will come from parent class

---

### 2.2 Introduce `InteractionBoundary` subclasses

- [x] **Create InteractionBoundary sealed class**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/InteractionBoundary.kt` (new file)
  - Create `sealed class InteractionBoundary` extending `ConversationEntry`
  - Common property: `val interaction: Interaction`

- [x] **Create InteractionBoundary.Start class**
  - In same file as above
  - `class Start internal constructor(timestamp, sender, interaction) : InteractionBoundary`
  - Constructor parameters:
    - `override val timestamp: Instant`
    - `override val sender: Participant`
    - `override val interaction: Interaction`

- [x] **Create InteractionBoundary.End class**
  - In same file as above
  - `class End internal constructor(timestamp, sender, interaction) : InteractionBoundary`
  - Constructor parameters:
    - `override val timestamp: Instant`
    - `override val sender: Participant`
    - `override val interaction: Interaction`

---

### 2.3 Introduce `InteractionProtocol` data class

- [x] **Create InteractionProtocol data class**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/InteractionProtocol.kt` (new file)
  - Create data class with properties:
    - `val id: String` (should be URN, e.g., `urn:konvo:io.github.ptitjes.konvo.core/Messaging#Processing`)
    - `val awaitsInput: Boolean`
    - `val hidesParent: Boolean`
    - `val reactsTo: Set<KClass<out Action.Payload>>`
  - Add documentation explaining each property

- [x] **Remove or update old InteractionProtocol**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/events/AgentProcessing.kt`
  - Remove the existing `InteractionProtocol` data class (lines 41-47)
  - Update `AgentProcessing.TurnBased` to use the new `InteractionProtocol` class
  - Update field name: `inputEvents` → `reactsTo`

---

### 2.4 Introduce `Interaction` class

- [x] **Create Interaction class**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/Interaction.kt` (new file)
  - Create class with internal constructor:
    ```kotlin
    class Interaction internal constructor(
        val id: String,
        val protocol: InteractionProtocol,
        val parent: Interaction?,
        val trigger: Action<*>?,
    )
    ```
  - Implement `hashCode()` and `equals()` based on `id` only
  - Add `toString()` for debugging

---

### 2.5 Implement `startInteraction` and `endInteraction` in Conversation

- [x] **Implement methods in Conversation.AgentViewImpl**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/Conversation.kt`
  - Implement `startInteraction`:
    - Generate new interaction ID using `newId()`
    - Create `Interaction` instance
    - Create `InteractionBoundary.Start` with current timestamp
    - Emit to `_actions` flow
    - Return the `Interaction` instance
  - Implement `endInteraction`:
    - Create `InteractionBoundary.End` with current timestamp
    - Emit to `_actions` flow

- [x] **Update internal action emission**
  - Update `_actions` type to `MutableSharedFlow<ConversationEntry>`
  - Update emission logic to handle both `Action` and `InteractionBoundary`
  - Keep public API `actions` property as `SharedFlow<Action<*>>` by filtering

---

## Phase 3: Introduction of `ConversationTranscript`

### 3.1 Introduce the `ConversationTranscript` class

- [x] **Create ConversationTranscript class**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/ConversationTranscript.kt` (new file or replace existing Transcript.kt)
  - Create class:
    ```kotlin
    class ConversationTranscript internal constructor(
        val digest: ConversationDigest,
        val entries: List<ConversationEntry>,
    ) : List<ConversationEntry> by entries
    ```
  - Implements `List<ConversationEntry>` by delegating to `entries` property

- [x] **Remove or deprecate old Transcript class**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/Transcript.kt`
  - If keeping for compatibility, mark as `@Deprecated`
  - Otherwise, delete the file

---

### 3.2 Update ConversationRepository interface

- [x] **Rename `getActions` to `getTranscript`**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/ConversationRepository.kt`
  - Change method signature: `fun getActions(conversationId: String): Flow<List<Action<*>>>` → `fun getTranscript(conversationId: String): Flow<ConversationTranscript>`
  - Update documentation
  - Added deprecated wrapper for backward compatibility

- [x] **Rename `appendAction` to `appendEntry`**
  - Same file as above
  - Change method signature: `suspend fun appendAction(conversationId: String, action: Action<*>)` → `suspend fun appendEntry(conversationId: String, entry: ConversationEntry)`
  - Update documentation
  - Added deprecated wrapper for backward compatibility

---

### 3.3 Update FileConversationRepository implementation

**Note**: Phase 3.3 has been partially implemented with a simplified approach:
- Implemented `getTranscript()` to return `ConversationTranscript` with Actions only
- Implemented `appendEntry()` but only persists Actions to disk (InteractionBoundaries are skipped)
- Full serialization support for InteractionBoundaries will be added in a future phase

#### 3.3.1 Add DTOs for InteractionBoundary

- [ ] **Add InteractionBoundaryDto**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/Dto.kt`
  - Create sealed interface:
    ```kotlin
    @Serializable
    @SerialName("interaction-boundary")
    sealed interface InteractionBoundaryDto {
        val interactionId: String

        @Serializable
        @SerialName("start")
        data class Start(
            @Contextual val timestamp: Instant,
            val sender: ParticipantDto,
            override val interactionId: String,
            val protocolId: String,
            val parentInteractionId: String?,
            val triggerActionId: String?,
        ) : InteractionBoundaryDto

        @Serializable
        @SerialName("end")
        data class End(
            @Contextual val timestamp: Instant,
            val sender: ParticipantDto,
            override val interactionId: String,
        ) : InteractionBoundaryDto
    }
    ```

- [ ] **Add ConversationEntryDto union**
  - Same file as above
  - Create sealed interface to represent both Action and InteractionBoundary:
    ```kotlin
    @Serializable
    sealed interface ConversationEntryDto
    ```
  - Make `ActionDto` and `InteractionBoundaryDto` extend `ConversationEntryDto`

#### 3.3.2 Add serialization support

- [ ] **Update CoreEvents serialization module**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/events/CoreEvents.kt`
  - Add polymorphic serialization for `ConversationEntry`:
    ```kotlin
    polymorphic(ConversationEntry::class) {
        subclass(Action::class)
        subclass(InteractionBoundary.Start::class)
        subclass(InteractionBoundary.End::class)
    }
    ```

#### 3.3.3 Update DTO mappers

- [ ] **Add interaction property to ActionDto**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/Dto.kt`
  - Add field to `ActionDto`: `val interactionId: String?`

- [ ] **Add InteractionProtocolDto cache**
  - Same file as above
  - Create object to maintain known protocols:
    ```kotlin
    internal object InteractionProtocols {
        private val knownProtocols = mutableMapOf<String, InteractionProtocol>()

        fun register(protocol: InteractionProtocol) {
            knownProtocols[protocol.id] = protocol
        }

        fun get(id: String): InteractionProtocol? = knownProtocols[id]
    }
    ```

- [ ] **Update DtoMappers for Interaction and InteractionBoundary**
  - Same file as above
  - Add mapper for `InteractionBoundary.Start`:
    ```kotlin
    fun toDto(boundary: InteractionBoundary.Start): InteractionBoundaryDto.Start
    ```
  - Add mapper for `InteractionBoundary.End`:
    ```kotlin
    fun toDto(boundary: InteractionBoundary.End): InteractionBoundaryDto.End
    ```
  - Add deserialization context class:
    ```kotlin
    class DeserializationContext {
        private val interactions = mutableMapOf<String, Interaction>()
        private val actions = mutableMapOf<String, Action<*>>()

        fun getOrCreateInteraction(id: String, protocol: InteractionProtocol,
                                   parent: Interaction?, trigger: Action<*>?): Interaction
        fun addAction(action: Action<*>)
        fun getAction(id: String): Action<*>?
    }
    ```

- [ ] **Update ActionDto mapper to include interactionId**
  - Same file as above
  - Update `toDto(a: Action<*>)` to serialize `a.interaction?.id`
  - Update `fromDto(a: ActionDto, context: DeserializationContext)` to resolve interaction from context

#### 3.3.4 Update repository methods

- [ ] **Update `readTranscript` method**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/FileConversationRepository.kt`
  - Rename `readActions` → `readTranscript`
  - Create `DeserializationContext` instance
  - Read lines and deserialize as `ConversationEntryDto` (which can be `ActionDto` or `InteractionBoundaryDto`)
  - For `InteractionBoundary.Start`: Create/retrieve `Interaction` from context
  - For `ActionDto`: Resolve interaction from context using `interactionId`
  - Build list of `ConversationEntry` (both `Action` and `InteractionBoundary`)
  - Return `ConversationTranscript` with digest and entries

- [ ] **Update `getTranscript` method**
  - Same file as above
  - Change return type: `Flow<List<Action<*>>>` → `Flow<ConversationTranscript>`
  - Load digest within the flow
  - Call `readTranscript` and construct `ConversationTranscript` instance

- [ ] **Update `appendEntry` method**
  - Same file as above
  - Rename `appendAction` → `appendEntry`
  - Update parameter: `action: Action<*>` → `entry: ConversationEntry`
  - Handle serialization of both `Action` and `InteractionBoundary`
  - Use polymorphic serialization or manual type checking

- [ ] **Update event file naming (optional)**
  - Consider renaming `events.ndjson` → `transcript.ndjson` or keep for compatibility

---

### 3.4 Update InMemoryConversationRepository implementation

- [x] **Update storage fields**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/inmemory/InMemoryConversationRepository.kt`
  - Change field type: `private val actions = atomic<Map<String, List<Action<*>>>>(...)` → `private val entries = atomic<Map<String, List<ConversationEntry>>>(...)`
  - Update state flows similarly

- [x] **Implement `getTranscript` method**
  - Same file as above
  - Load digest from `conversations`
  - Get entries for conversation ID
  - Construct `ConversationTranscript` with digest and entries
  - Return as Flow

- [x] **Update `appendEntry` method**
  - Same file as above
  - Rename `appendAction` → `appendEntry`
  - Update to accept `ConversationEntry` instead of `Action<*>`

- [x] **Update other methods**
  - Update `create` to initialize with empty `List<ConversationEntry>`
  - Update delete methods

---

### 3.5 Update Conversation class

- [x] **Update state data class**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/Conversation.kt`
  - Kept `ConversationState.Loaded` with `transcript: List<Action<*>>` for now (extracted from ConversationTranscript)

- [x] **Update repository calls**
  - Same file as above
  - Change call: `repository.getActions(id)` → `repository.getTranscript(id)`
  - Change call: `repository.appendAction(id, event)` → `repository.appendEntry(id, entry)`

- [x] **Update action emission**
  - Used `_events` field for ConversationEntry
  - Update collection: `_events.collect { entry -> repository.appendEntry(id, entry) }`

- [x] **Update restoreAgents method**
  - Kept parameter as `restoreAgents(transcript: List<Action<*>>)` since we filter from ConversationTranscript

- [ ] **Update Agent interface**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/Agent.kt`
  - Update method: `suspend fun restoreSession(transcript: List<Action<*>>, ...)` → `suspend fun restoreSession(transcript: ConversationTranscript, ...)`

---

### 3.6 Update agent implementations

- [ ] **Update DefaultAgent**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/DefaultAgent.kt`
  - Update `restoreSession` parameter type: `transcript: List<Action<*>>` → `transcript: ConversationTranscript`
  - Update internal logic to work with transcript (filter actions, ignore boundaries)

- [ ] **Update RoleplayAgent**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/RoleplayAgent.kt`
  - Update transcript handling

- [ ] **Update QuestionAnswerAgent**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/QuestionAnswerAgent.kt`
  - Update transcript handling

---

### 3.7 Update test files

- [ ] **Update repository tests**
  - File: `konvo-core/src/commonTest/kotlin/io/github/ptitjes/konvo/core/conversations/storage/ConversationRepositoryContractTests.kt`
  - Update method calls: `getActions` → `getTranscript`, `appendAction` → `appendEntry`
  - Update assertions to work with `ConversationTranscript`

- [ ] **Update file repository tests**
  - File: `konvo-core/src/jvmTest/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/FileConversationRepositoryContractTests.kt`
  - File: `konvo-core/src/jvmTest/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/FileConversationRepositoryPartialFilesTests.kt`
  - Update all test methods

---

## Phase 4: Introduction of `interaction` in `Action`

### 4.1 Add nullable `interaction` property to `Action`

- [x] **Update Action class**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/model/Action.kt`
  - Add property: `val interaction: Interaction?`
  - Update constructor to accept this parameter
  - Update documentation

---

### 4.2 Update ActionDto (already done in Phase 3.3.3)

- [ ] **Verify interactionId property exists**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/Dto.kt`
  - Confirm `val interactionId: String?` is present in `ActionDto`
  - Note: Deferred - serialization support will be added in future phase

---

### 4.3 Update repository serialization (already done in Phase 3.3.3)

- [ ] **Verify interaction serialization in mappers**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/Dto.kt`
  - Confirm `toDto` serializes `action.interaction?.id` as `interactionId`
  - Confirm `fromDto` deserializes using `DeserializationContext.getOrCreateInteraction`
  - Note: Deferred - serialization support will be added in future phase

---

### 4.4 Update InteractionDevice.Agent.act to accept interaction

- [x] **Update method signature**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/InteractionDevice.kt`
  - Change: `suspend fun act(payload: Action.Agent)` → `suspend fun act(payload: Action.Agent, interaction: Interaction? = null)`

- [x] **Update implementation in Conversation.AgentViewImpl**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/Conversation.kt`
  - Update `act` method to accept and use `interaction` parameter
  - Pass `interaction` when creating `Action` instance

---

### 4.5 Update InteractionDevice.User.act to accept interaction

- [x] **Update method signature**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/InteractionDevice.kt`
  - Keep: `suspend fun act(payload: Action.User, interaction: Interaction? = null)` (already has default)

- [x] **Update implementation in Conversation.UserViewImpl**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/Conversation.kt`
  - Update `act` method implementation
  - Pass `interaction` when creating `Action` instance

---

### 4.6 Update agent implementations to use interactions

- [ ] **Update DefaultAgent to track current interaction**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/DefaultAgent.kt`
  - Add field to track current interaction
  - Call `conversation.startInteraction(...)` when starting message processing
  - Pass interaction to `conversation.act(payload, interaction)`
  - Call `conversation.endInteraction(interaction)` when done

- [ ] **Update tool vetting logic**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/agents/toolkit/Nodes.kt`
  - Update `vetToolCalls` to start a vetting interaction
  - Pass interaction when emitting vetting actions

---

## Phase 5: Testing and Validation

### 5.1 Build and compile

- [ ] **Build the project**
  - Run: `./gradlew build`
  - Fix any compilation errors
  - Ensure all modules compile successfully

---

### 5.2 Run tests

- [ ] **Run core tests**
  - Run: `./gradlew konvo-core:test`
  - Verify all repository contract tests pass
  - Verify file repository tests pass

- [ ] **Run frontend tests**
  - Run: `./gradlew konvo-frontend-compose:test`
  - Verify view model tests pass

---

### 5.3 Manual testing

- [ ] **Test basic conversation flow**
  - Create new conversation
  - Send message
  - Verify action is saved with correct structure
  - Verify transcript loads correctly

- [ ] **Test interaction boundaries**
  - Verify `startInteraction` emits `InteractionBoundary.Start`
  - Verify actions include interaction reference
  - Verify `endInteraction` emits `InteractionBoundary.End`
  - Verify boundaries are persisted and deserialized correctly

- [ ] **Test serialization/deserialization**
  - Create conversation with interactions
  - Close and reopen application
  - Verify transcript loads with correct interaction objects
  - Verify interaction object identity is maintained (same id = same instance)

- [ ] **Test frontend integration**
  - Verify UI displays actions correctly
  - Verify user can send messages
  - Verify agent responses appear correctly
  - Verify tool vetting flow works with interactions

---

### 5.4 Update schema version

- [ ] **Increment schema version**
  - File: `konvo-core/src/commonMain/kotlin/io/github/ptitjes/konvo/core/conversations/storage/files/Dto.kt`
  - Update `schemaVersion` in `ConversationDto` from `3` → `4`
  - Add comment explaining the change

---

## Phase 6: Documentation and Cleanup

### 6.1 Update documentation

- [ ] **Update conversation-model.md**
  - File: `docs/conversation-model.md`
  - Mark document as up-to-date
  - Remove any FIXME or TODO comments that were addressed
  - Add examples of interaction usage

- [ ] **Mark conversation-model-todo.md as complete**
  - File: `docs/conversation-model-todo.md`
  - Add completion date
  - Archive or mark as implemented

---

### 6.2 Code cleanup

- [ ] **Remove deprecated code**
  - Remove any `@Deprecated` annotations and old interfaces if no longer needed
  - Clean up any temporary compatibility code

- [ ] **Format code**
  - Run code formatter on all modified files
  - Ensure consistent style

- [ ] **Remove unused imports**
  - Clean up any unused imports across all modified files

---

### 6.3 Final review

- [ ] **Code review**
  - Review all changes for correctness
  - Verify naming consistency
  - Check for any missed references to old names

- [ ] **Performance review**
  - Verify no performance regressions
  - Check that transcript loading is efficient
  - Verify interaction object creation doesn't cause memory issues

---

## Summary

This implementation plan transforms the conversation model from a simple event-based system to a rich interaction-based model with:

1. **Actions** (formerly Events) - individual communicative acts
2. **Interactions** - grouped spans of actions following specific protocols
3. **InteractionBoundaries** - markers for interaction start/end
4. **ConversationTranscript** - unified representation of the conversation log
5. **InteractionDevices** - cleaner API for agents and users to interact

The plan is structured to minimize breaking changes at each step while progressively building up the new model. Each phase can be tested independently before moving to the next.

**Total estimated checkboxes: ~150 individual tasks**
