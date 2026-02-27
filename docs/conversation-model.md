# Conversation Model

This document outlines the conversation model used by Konvo,
detailing how conversations are structured and how they are handled.

## Concepts

A **conversation** describes a series of interactions between multiple conversation **participants**,
which can be users or agents.

### Interactive Agents and Actions

Agents in Konvo are said to be **interactive**.

Whereas Koog agents can be viewed as functions that take a user input and return an agent response,
Konvo agents communicate together with users and other agents via communicative acts called **actions**.

Actions can convey various **payloads**:

- agent and user presence notifications
- agent capabilities and configuration changes
- multimodal messages sent by users and agents
- tool usage vetting, approval and notifications
- artifact creations and updates
- etc.

[//]: # (TODO Action payload types have Description annotations)

[//]: # (TODO Give plenty of example action payloads: from classical `message` actions, to `vetting` actions, etc.)

### Interactions and Protocols

Interactive agents define **interaction protocols** to advertise how they can interact with other participants
— specifically, whether they are stalled awaiting input, and what actions they can react to.

In addition to actions,
agents can emit **interaction boundaries** to denote the start and end of an **interaction**
(e.g., processing a multimodal message request, or vetting tool usage).
Start and end boundaries denote spans over the conversation transcript,
grouping actions together into **interactions**.

Each interaction is defined to follow a specific interaction protocol, defined and advertised by the agent.
All participants know the interaction protocols in advance
or can discover them through the actions emitted by the agent.

> **Example:** We can define a simple Message Request/Response interaction pattern as follows:
> - An `availability` protocol, that advertises the agent's ability to handle `message` actions
> - A `processing` protocol that advertises the agent's current processing of a message
>
> During the `availability` protocol, the agent emits `capability` actions,
> to describe what it can do with the messages it receives.
> During the `processing` protocol, the agent emits `processing` actions,
> to describe what it is doing with the message it is currently processing.
> Eventually, the agent will itself emit `message` actions (and maybe more), to convey the outcome of the processing.
> Ultimately, the agent will emit a `completion` action, to indicate that it has finished processing the message.

> **Example:** We easily plug in a simple Tool Usage Vetting protocol definition.
> - A `vetting` protocol, that advertises the agent's awaiting `approval` actions
>
> At the start of the `vetting` protocol, the agent emits `vetting` actions,
> to describe the tool calls it is needing vetting for.
> In response, the user is expected to emit `approval` actions,
> to describe whether they approve of the tool calls.
> Eventually, the agent receives approvals for all the tool calls it needs vetting.
> The agent then ends the interaction and resumes its normal processing.

Interactions can be nested: every interaction may have a **parent interaction**,
and their interaction protocol may define some modality over their transitive parent interactions,
i.e., whether and how the agent still reacts to the parent interactions' advertised actions.

> **Example:** In the above example of a simple Message Request/Response interaction,
> the `processing` interaction is nested within the `availability` interaction.
> If we define the `processing` interaction as hiding its parent interaction,
> then we signify that the user should not emit other `message` actions until the `processing` interaction is complete.

> **Example:** In the simple Tool Usage Vetting protocol definition above,
> we can specify that the interaction won't hide its parent interaction (say a `processing` interaction),
> therefore signifying that the user can still emit other `cancel` actions during the `vetting` interaction.

> **Note:** Using modality, we can easily model turn-based interaction patterns, but we can also model
> parallel interaction patterns, like request queuing or concurrent request handling.
>
> For example, we can define interaction protocols for a collaborative problem-solving task,
> where multiple agents and users work together to solve a problem.
> Each agent would (maybe take turns to) propose a solution,
> and all the participants would review and vote on each proposed solution.

Interaction protocols are defined to be **self-contained**, with all necessary information included within the protocol.
This ensures that participants can understand and follow the protocol without external references.

Specifically, an interaction protocol defines:

- whether the agent is stalled awaiting input from other participants
- the modality of the interaction w.r.t. its parent interactions
- what actions it can react to: defined by the payload types (urn) of the actions it can react to
  and for each action type, a description of the intended effect on the agent.

> **Example:** Simple Message Request and Response protocols definition

[//]: # (FIXME: what is the minimum that we need to define an interaction protocol?)

[//]: # (FIXME: fixup the DSLs bellow)

```kotlin
val presence by interactionProtocol<ConversationControl.Invite>(awaitsInput = false) {
    emitsOnStart<AgentPresence.Joining> { "The agent is joining the conversation." }
    emitsOnEnd<AgentPresence.Leaving> { "The agent is leaving the conversation." }
    emits<AgentCapabilities.Messaging> { "The agent is ready to receive messages." }
}

val available by interactionProtocol<AgentCapabilities.Messaging>(awaitsInput = true) {
    action<Messaging.Message> { "Requests the processing of a given message." }
}

val processing by interactionProtocol<Messaging.Message>(
    awaitsInput = false,
    hidesParent = true,
) {
    action<AgentProcessing.Cancellation> { "Will cancel the processing." }
}
```

> **Example:** Simple Tool Usage Vetting protocols definition

```kotlin
val vetting by interactionProtocol<ToolUsage.Vetting>(awaitsInput = true) {
    action<ToolUsage.Approval> { "Will approve the given set of tool calls." }
}
```

The interaction protocols give guidance on how to interact with an agent
and how to interpret the actions emitted by the agent.

[//]: # (TODO Simple Message Request and Response example)

[//]: # (TODO Tool Usage Vetting example)

[//]: # (TODO talk about modality)

### Conversation

A **conversation** is an append-only log of interactions between multiple participants.

## Logical Representation

A `Conversation` allows access to both the transcript of actions that occured in the past,
and interaction devices for the user and for the running agents.

### Transcript

A `ConversationTranscript` is an append-only list of conversation entries, which can be:

- actions with payload
- interaction boundary markers

```kotlin
interface ConversationTranscript : List<ConversationEntry> {
  val digest: ConversationDigest
  val entries: List<ConversationEntry>
}
```

All entries all have a timestamp and a sender.

```kotlin
sealed interface ConversationEntry {
  val timestamp: Instant
  val sender: Participant
}
```

### Actions

Actions are represented as `Action` instances and have:

- a unique id
- a timestamp
- a sender participant (internally identified by its id)
- an optional interaction (internally identified by its id)
- an optional set of recipient participants (internally identified by their id)
- a payload

```kotlin
class Action<P : Action.Payload> : ConversationEntry {
  val id: Any
  val interaction: Interaction?
  val recipients: Set<Participant>?
  val payload: P

  interface Payload
  interface Agent : Payload
  interface User : Payload
}
```

### Interaction Boundaries

Interaction boundary markers can be:

- a start marker containing:
    - the unique id of the interaction it marks the start of
    - the interaction protocol the interaction follows (internally identified by its id)
    - the optional parent interaction it is nested within (internally identified by its id),
    - the optional trigger action that initiated the interaction (internally identified by its id)
- an end marker containing:
    - the unique id of the interaction it marks the end of

```kotlin
sealed interface InteractionBoundary : ConversationEntry {
  val interaction: Interaction

  class Start(
    override val interaction: Interaction,
  ) : InteractionBoundary
  class Stop(
    override val interaction: Interaction,
  ) : InteractionBoundary
}
```

### Interactions

Interactions are represented as `Interaction` instances and have:

- a unique id
- an interaction protocol (internally identified by its id)
- an optional parent interaction (internally identified by its id)
- an optional trigger action, which is the event that initiated the interaction (internally identified by its id)

```kotlin
class Interaction internal constructor(
  val id: Any,
  val protocol: InteractionProtocol,
  val parent: Interaction?,
  val trigger: Action<*>?,
) {
    // Hash code and equals are implemented on id.
}
```

### Interaction Protocols

```kotlin
data class InteractionProtocol {
  val id: String // should be a URN (e.g., urn:konvo:io.github.ptitjes.konvo.roleplay/Roleplay#Acting)
  val awaitsInput: Boolean
  val hidesParent: Boolean
  val reactsTo: Set<KClass<Action<*>>>
  // should all identified be a URN (e.g., urn:konvo:io.github.ptitjes.konvo.core/Messaging.Message)
}
```

[//]: # (TODO identify the needed modalities. Do we need an enum of them instead of `hidesParent`?)

### Interaction Devices

The `InteractionDevice.Agent` and `InteractionDevice.User` have the following shape:

```kotlin
interface InteractionDevice {
  val participant: Participant

  val actions: SharedFlow<Action<*>>

  interface Agent : InteractionDevice {
    override val participant: Participant.Agent

    suspend fun startInteraction(
      protocol: InteractionProtocol,
      parent: Interaction? = null,
      trigger: Action<*>? = null
    ): Interaction

    suspend fun endInteraction(interaction: Interaction)

    suspend fun act(payload: Action.Agent, interaction: Interaction)
  }

  interface User : InteractionDevice {
    override val participant: Participant.User

    suspend fun act(payload: Action.User, interaction: Interaction? = null)
  }
}
```

## JSON-ND Concrete Representation


