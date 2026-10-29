# Real-time protocol

Connect to `ws://localhost:8080/ws?token=<access-jwt>`. This is a native WebSocket endpoint (not STOMP), chosen to relay binary Yjs updates without a browser-side protocol dependency. A client must join before sending other messages.

## Client → server

| Type/frame | Payload | Server behavior |
| --- | --- | --- |
| JSON `join` | `{ "roomId": "PC-7X4K9" }` | Authorizes membership and subscribes the socket to the room. |
| JSON `leave` | none | Removes the socket from the room. |
| Binary frame | Yjs update bytes | Requires edit permission; persists latest bytes and relays to peers. |
| JSON `doc.update` | `{ "payload": "base64" }` | Compatibility form of binary document update. |
| JSON `awareness` | `{ "payload": {...} }` | Relays cursors/selections to the room. |
| REST `POST /rooms/{id}/messages` | `{ "content": "…" }` | Persists then emits `chat`. |
| REST `POST /rooms/{id}/run` | source, language, stdin | Emits `run.started`, then `run.finished`. |
| REST `PATCH /rooms/{id}/permissions` | edit/lock flags | Interviewer-only; emits `permission.changed`. |
| REST `PATCH /rooms/{id}/language` | `{ "language": "JAVA\|PYTHON\|CPP" }` | Interviewer or an editable candidate; emits `language.changed`. |

## Server → client

Every JSON event has `{ "type": "…", "data": … }`.

- `participant.list`: current participants and presence.
- `doc.update`: a compatibility JSON document update; binary peers receive the original binary frame.
- `awareness`: peer cursor/selection data.
- `chat`: persisted message.
- `run.started`, `run.finished`: submission lifecycle/result.
- `permission.changed`: current room permission state.
- `language.changed`: current room state after its language changes. The client initiating the change replaces only an empty or unchanged starter document; other clients retain their current document.
- `session.ended`: session is no longer editable.
- `error`: a non-fatal protocol error.

The server repeats authorization for REST and WebSocket messages. In particular, candidate document updates are rejected after candidate editing is disabled or the editor is locked.
