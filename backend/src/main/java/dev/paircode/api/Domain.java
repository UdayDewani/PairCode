package dev.paircode.api;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.util.UUID;

enum Role { INTERVIEWER, CANDIDATE }
enum RoomStatus { ACTIVE, ENDED }
enum Difficulty { EASY, MEDIUM, HARD }
enum SubmissionStatus { QUEUED, RUNNING, SUCCESS, COMPILE_ERROR, RUNTIME_ERROR, TIME_LIMIT_EXCEEDED }

@Entity @Table(name="users")
class AppUser {
  @Id UUID id; @Column(nullable=false, unique=true) String email; @Column(name="password_hash",nullable=false) String passwordHash;
  @Column(name="display_name",nullable=false) String displayName; @Column(name="preferred_language",nullable=false) String preferredLanguage="PYTHON";
  @Column(name="created_at") Instant createdAt=Instant.now(); @Column(name="updated_at") Instant updatedAt=Instant.now();
  protected AppUser(){} AppUser(String email,String passwordHash,String displayName){id=UUID.randomUUID();this.email=email.toLowerCase();this.passwordHash=passwordHash;this.displayName=displayName;}
}
@Entity @Table(name="refresh_tokens")
class RefreshToken { @Id UUID id=UUID.randomUUID(); @Column(name="user_id") UUID userId; @Column(name="token_hash") String tokenHash; @Column(name="expires_at") Instant expiresAt; @Column(name="revoked_at") Instant revokedAt; protected RefreshToken(){} RefreshToken(UUID userId,String h,Instant e){this.userId=userId;tokenHash=h;expiresAt=e;} }
@Entity @Table(name="problems")
class Problem { @Id UUID id; @Column(name="number") String number; String title,slug,topic; @Enumerated(EnumType.STRING) Difficulty difficulty; @Column(columnDefinition="text") String description; @Column(name="constraints_text",columnDefinition="text") String constraintsText; @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition="jsonb") String examples="[]"; @JdbcTypeCode(SqlTypes.JSON) @Column(name="starter_code",columnDefinition="jsonb") String starterCode="{}"; @Column(name="acceptance_rate", precision=5, scale=2) java.math.BigDecimal acceptanceRate; protected Problem(){} }
@Entity @Table(name="rooms")
class Room { @Id UUID id=UUID.randomUUID(); @Column(name="room_code") String roomCode; String name; @Column(name="owner_id") UUID ownerId; @Column(name="problem_id") UUID problemId; String language="PYTHON"; @Enumerated(EnumType.STRING) RoomStatus status=RoomStatus.ACTIVE; @Column(name="candidate_can_edit") boolean candidateCanEdit=true; @Column(name="editor_locked") boolean editorLocked; @Column(name="document_state") byte[] documentState; @Column(name="created_at") Instant createdAt=Instant.now(); @Column(name="ended_at") Instant endedAt; protected Room(){} }
@Entity @Table(name="room_participants")
class RoomParticipant { @Id UUID id=UUID.randomUUID(); @Column(name="room_id") UUID roomId; @Column(name="user_id") UUID userId; @Enumerated(EnumType.STRING) Role role; @Column(name="joined_at") Instant joinedAt=Instant.now(); @Column(name="left_at") Instant leftAt; @Column(name="removed") boolean removed; protected RoomParticipant(){} RoomParticipant(UUID r,UUID u,Role role){roomId=r;userId=u;this.role=role;} }
@Entity @Table(name="chat_messages")
class ChatMessage { @Id UUID id=UUID.randomUUID(); @Column(name="room_id") UUID roomId; @Column(name="author_id") UUID authorId; @Column(columnDefinition="text") String content; String type="CHAT"; @Column(name="created_at") Instant createdAt=Instant.now(); protected ChatMessage(){} ChatMessage(UUID room,UUID author,String content,String type){roomId=room;authorId=author;this.content=content;this.type=type;} }
@Entity @Table(name="submissions")
class Submission { @Id UUID id=UUID.randomUUID(); @Column(name="room_id") UUID roomId; @Column(name="user_id") UUID userId; @Column(name="problem_id") UUID problemId; String language; @Column(name="source_code",columnDefinition="text") String sourceCode; @Column(columnDefinition="text") String stdin; @Enumerated(EnumType.STRING) SubmissionStatus status=SubmissionStatus.QUEUED; @Column(columnDefinition="text") String stdout,stderr; @Column(name="compile_error",columnDefinition="text") String compileError; @Column(name="exit_code") Integer exitCode; @Column(name="execution_time_ms") Long executionTimeMs; @Column(name="memory_bytes") Long memoryBytes; Integer score; @Column(name="created_at") Instant createdAt=Instant.now(); protected Submission(){} }
@Entity @Table(name="test_cases")
class TestCase { @Id UUID id; @Column(name="problem_id") UUID problemId; @Column(columnDefinition="text") String input; @Column(name="expected_output",columnDefinition="text") String expectedOutput; @Column(name="is_hidden") boolean hidden; int ordinal; protected TestCase(){} }
@Entity @Table(name="test_results")
class TestResult { @Id UUID id=UUID.randomUUID(); @Column(name="submission_id") UUID submissionId; @Column(name="test_case_id") UUID testCaseId; boolean passed; @Column(name="runtime_ms") Long runtimeMs; @Column(name="actual_output",columnDefinition="text") String actualOutput; protected TestResult(){} TestResult(UUID s,UUID t,boolean pass,long runtime,String output){submissionId=s;testCaseId=t;passed=pass;runtimeMs=runtime;actualOutput=output;} }
@Entity @Table(name="code_snapshots")
class CodeSnapshot { @Id UUID id=UUID.randomUUID(); @Column(name="room_id") UUID roomId; @Column(name="author_id") UUID authorId; @Column(name="source_code",columnDefinition="text") String sourceCode; String reason; @Column(name="created_at") Instant createdAt=Instant.now(); protected CodeSnapshot(){} CodeSnapshot(UUID room,UUID author,String source,String reason){roomId=room;authorId=author;sourceCode=source;this.reason=reason;} }
@Entity @Table(name="sessions")
class SessionRecord { @Id UUID id=UUID.randomUUID(); @Column(name="room_id") UUID roomId; @Column(name="owner_id") UUID ownerId; @Column(name="started_at") Instant startedAt=Instant.now(); @Column(name="ended_at") Instant endedAt; @Column(name="duration_seconds") Long durationSeconds; Integer score; protected SessionRecord(){} SessionRecord(UUID room,UUID owner){roomId=room;ownerId=owner;} }
@Entity @Table(name="session_notes")
class SessionNote { @Id UUID id=UUID.randomUUID(); @Column(name="session_id") UUID sessionId; @Column(name="author_id") UUID authorId; @Column(columnDefinition="text") String content; @Column(name="created_at") Instant createdAt=Instant.now(); protected SessionNote(){} SessionNote(UUID sessionId,UUID authorId,String content){this.sessionId=sessionId;this.authorId=authorId;this.content=content;} }
