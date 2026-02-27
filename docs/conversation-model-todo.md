# Conversation Model TODO list

## Initial refactorings

- [ ] Rename `Event` to `Action`
  - Also rename the properties referencing `Event` adequately
  - Also rename the Dto classes associated
- [ ] Rename `ConversationAgentView` to `InteractionDevice.Agent`
- [ ] Rename `ConversationUserView` to `InteractionDevice.User`
- [ ] Rename `InteractionDevice.Agent.send(...)` to `act`
- [ ] Rename `InteractionDevice.User.send(...)` to `act`
- [ ] Add `InteractionDevice.Agent.startInteraction(...)`
- [ ] Add `InteractionDevice.Agent.endInteraction(...)`

## Interaction type introduction

- [ ] Introduce the `ConversationEntry` sealed class hierarchy:
    - [ ] `InteractionBoundary.{Start, End}` subclasses
    - [ ] make `Action` a `ConversationEntry` subclass
- [ ] Introduce the `InteractionProtocol` data class
- [ ] Introduce the `Interaction` class

## Introduction of `ConversationTranscript`

- [ ] Introduce the `ConversationTranscript` interface
- Modify the `ConversationRepository` interface:
    - [ ] Rename `getEvents` to `getTranscript` and make it return a `Flow<ConversationTranscript>`

- Update the repository implementations:
    - Repository should serialize entries as follows:
        - `Action.interaction` is serialized as the unique identifier of the `Interaction`
        - `InteractionBoundary.Start.id` is the unique identifier of the `Interaction`
        - `InteractionBoundary.Start.protocol` is serialized as the unique identifier of the `InteractionProtocol`
        - `InteractionBoundary.Start.parent` is serialized as the unique identifier of the `Interaction`, if non-null
        - `InteractionBoundary.Start.trigger` is serialized as the unique identifier of the `Action`, if non-null
        - `InteractionBoundary.End.id` is the unique identifier of the `Interaction`

## Introduction of `interaction` in `Action`

- [ ] Add the nullable `interaction` property to `Action`
- [ ] Add the nullable `interactionId` property to `ActionDto`
- [ ] Update the repository implementations
  - While deserializing a transcript, maintain a map of created interaction objects per id
  - When deserializing an action, resolve the `interaction` property using the `interactionId`
