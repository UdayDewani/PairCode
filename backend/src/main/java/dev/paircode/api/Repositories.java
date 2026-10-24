package dev.paircode.api;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

interface UserRepository extends JpaRepository<AppUser,UUID> { Optional<AppUser> findByEmail(String email); }
interface RefreshTokenRepository extends JpaRepository<RefreshToken,UUID> { Optional<RefreshToken> findByTokenHash(String tokenHash); }
interface ProblemRepository extends JpaRepository<Problem,UUID> { Page<Problem> findByTitleContainingIgnoreCaseOrTopicContainingIgnoreCase(String title,String topic,Pageable pageable); }
interface RoomRepository extends JpaRepository<Room,UUID> { Optional<Room> findByRoomCode(String roomCode); List<Room> findByOwnerIdOrderByCreatedAtDesc(UUID ownerId); }
interface ParticipantRepository extends JpaRepository<RoomParticipant,UUID> { Optional<RoomParticipant> findByRoomIdAndUserId(UUID roomId,UUID userId); List<RoomParticipant> findByRoomId(UUID roomId); }
interface ChatRepository extends JpaRepository<ChatMessage,UUID> { List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(UUID roomId); }
interface SubmissionRepository extends JpaRepository<Submission,UUID> { List<Submission> findByRoomIdOrderByCreatedAtDesc(UUID roomId); }
interface TestCaseRepository extends JpaRepository<TestCase,UUID> { List<TestCase> findByProblemIdOrderByOrdinal(UUID problemId); }
interface TestResultRepository extends JpaRepository<TestResult,UUID> { List<TestResult> findBySubmissionId(UUID submissionId); }
interface SnapshotRepository extends JpaRepository<CodeSnapshot,UUID> { List<CodeSnapshot> findByRoomIdOrderByCreatedAtAsc(UUID roomId); }
interface SessionRepository extends JpaRepository<SessionRecord,UUID> { Optional<SessionRecord> findByRoomId(UUID roomId); List<SessionRecord> findByOwnerIdOrderByStartedAtDesc(UUID ownerId); }
interface NoteRepository extends JpaRepository<SessionNote,UUID> { List<SessionNote> findBySessionIdOrderByCreatedAtAsc(UUID sessionId); }
