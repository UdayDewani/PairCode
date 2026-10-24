# PairCode data model

```mermaid
erDiagram
  USERS ||--o{ REFRESH_TOKENS : owns
  USERS ||--o{ ROOMS : owns
  USERS ||--o{ ROOM_PARTICIPANTS : joins
  ROOMS ||--o{ ROOM_PARTICIPANTS : contains
  PROBLEMS ||--o{ ROOMS : selected_for
  PROBLEMS ||--o{ TEST_CASES : defines
  ROOMS ||--o{ SUBMISSIONS : has
  USERS ||--o{ SUBMISSIONS : makes
  SUBMISSIONS ||--o{ TEST_RESULTS : produces
  TEST_CASES ||--o{ TEST_RESULTS : evaluates
  ROOMS ||--o{ CODE_SNAPSHOTS : captures
  ROOMS ||--o{ CHAT_MESSAGES : contains
  ROOMS ||--|| SESSIONS : persists_as
  SESSIONS ||--o{ SESSION_NOTES : includes
```

All primary keys are UUIDs. Room IDs are separately stored as human-shareable `PC-XXXXX` codes. Hidden test inputs and expected values never leave the server; only the boolean/runtime result is serialised.
