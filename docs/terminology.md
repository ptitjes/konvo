# Conversation Terminology

## Participants

- A conversation can have two or more participants.
- A participant is either a user or a bot.

## Events

- An event is an element of conversation sent by a participant.
- An event can be a message or any other type of event that conveys information.
- Example of events:
  - message (with attachments)
  - acknowledgement of a message
  - tool use approval request
  - tool use approval response (either rejected or approved)
  - tool use notification (either successful or failed)
  - etc.
- An event has a timestamp
- An event has an issuer participant (either a user or a bot)
- An event has one or more recipient participants (either users or bots)

## Interactions

- An interaction is a sequence of events that occur in a conversation.
- An event can spawn an interaction.
- An event can complete an interaction.
- An interaction can denote the need for a participant to take action.
