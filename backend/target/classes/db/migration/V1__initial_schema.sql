CREATE TABLE users (
  id UUID PRIMARY KEY, email VARCHAR(320) NOT NULL UNIQUE, password_hash VARCHAR(100) NOT NULL,
  display_name VARCHAR(100) NOT NULL, preferred_language VARCHAR(20) NOT NULL DEFAULT 'PYTHON',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE refresh_tokens (id UUID PRIMARY KEY, user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE, token_hash VARCHAR(128) NOT NULL UNIQUE, expires_at TIMESTAMPTZ NOT NULL, revoked_at TIMESTAMPTZ);
CREATE TABLE problems (
  id UUID PRIMARY KEY, number VARCHAR(12) NOT NULL UNIQUE, title VARCHAR(200) NOT NULL, slug VARCHAR(220) NOT NULL UNIQUE,
  difficulty VARCHAR(12) NOT NULL CHECK (difficulty IN ('EASY','MEDIUM','HARD')), topic VARCHAR(80) NOT NULL, description TEXT NOT NULL,
  constraints_text TEXT, examples JSONB NOT NULL DEFAULT '[]', starter_code JSONB NOT NULL DEFAULT '{}', acceptance_rate NUMERIC(5,2) NOT NULL DEFAULT 0, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE rooms (
  id UUID PRIMARY KEY, room_code VARCHAR(16) NOT NULL UNIQUE, name VARCHAR(200) NOT NULL, owner_id UUID NOT NULL REFERENCES users(id),
  problem_id UUID REFERENCES problems(id), language VARCHAR(20) NOT NULL DEFAULT 'PYTHON', status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE' CHECK(status IN ('ACTIVE','ENDED')),
  candidate_can_edit BOOLEAN NOT NULL DEFAULT true, editor_locked BOOLEAN NOT NULL DEFAULT false, document_state BYTEA, created_at TIMESTAMPTZ NOT NULL DEFAULT now(), ended_at TIMESTAMPTZ
);
CREATE TABLE room_participants (
  id UUID PRIMARY KEY, room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE, user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  role VARCHAR(16) NOT NULL CHECK(role IN ('INTERVIEWER','CANDIDATE')), joined_at TIMESTAMPTZ NOT NULL DEFAULT now(), left_at TIMESTAMPTZ, UNIQUE(room_id,user_id)
);
CREATE TABLE test_cases (
  id UUID PRIMARY KEY, problem_id UUID NOT NULL REFERENCES problems(id) ON DELETE CASCADE, input TEXT NOT NULL, expected_output TEXT NOT NULL, is_hidden BOOLEAN NOT NULL DEFAULT false, ordinal INTEGER NOT NULL
);
CREATE TABLE submissions (
  id UUID PRIMARY KEY, room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE, user_id UUID NOT NULL REFERENCES users(id), problem_id UUID REFERENCES problems(id),
  language VARCHAR(20) NOT NULL, source_code TEXT NOT NULL, stdin TEXT, status VARCHAR(32) NOT NULL, stdout TEXT, stderr TEXT, compile_error TEXT, exit_code INTEGER, execution_time_ms BIGINT, memory_bytes BIGINT, score INTEGER, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE test_results (
  id UUID PRIMARY KEY, submission_id UUID NOT NULL REFERENCES submissions(id) ON DELETE CASCADE, test_case_id UUID NOT NULL REFERENCES test_cases(id), passed BOOLEAN NOT NULL, runtime_ms BIGINT, actual_output TEXT
);
CREATE TABLE code_snapshots (
  id UUID PRIMARY KEY, room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE, author_id UUID REFERENCES users(id), source_code TEXT NOT NULL, reason VARCHAR(32) NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE chat_messages (
  id UUID PRIMARY KEY, room_id UUID NOT NULL REFERENCES rooms(id) ON DELETE CASCADE, author_id UUID REFERENCES users(id), content TEXT NOT NULL, type VARCHAR(16) NOT NULL DEFAULT 'CHAT', created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE sessions (
  id UUID PRIMARY KEY, room_id UUID NOT NULL UNIQUE REFERENCES rooms(id) ON DELETE CASCADE, owner_id UUID NOT NULL REFERENCES users(id), started_at TIMESTAMPTZ NOT NULL DEFAULT now(), ended_at TIMESTAMPTZ, duration_seconds BIGINT, score INTEGER
);
CREATE TABLE session_notes (
  id UUID PRIMARY KEY, session_id UUID NOT NULL REFERENCES sessions(id) ON DELETE CASCADE, author_id UUID NOT NULL REFERENCES users(id), content TEXT NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_room_participants_user ON room_participants(user_id);
CREATE INDEX idx_rooms_owner_status ON rooms(owner_id,status);
CREATE INDEX idx_problems_filter ON problems(topic,difficulty);
CREATE INDEX idx_submissions_room ON submissions(room_id,created_at DESC);
CREATE INDEX idx_chat_room_created ON chat_messages(room_id,created_at);
CREATE INDEX idx_snapshots_room_created ON code_snapshots(room_id,created_at);
