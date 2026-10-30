package dev.paircode.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Executes the checked-in reference solution and the untouched V7 starter for every problem and
 * language. This catches input-format mismatches between the starter contract, the test cases,
 * and the judge's stdin/stdout execution.
 */
class ReferenceSolutionTest {
  private static final List<String> LANGUAGES = List.of("PYTHON", "JAVA", "CPP");
  private static final Map<String, String> EXTENSIONS = Map.of(
      "PYTHON", "py",
      "JAVA", "java",
      "CPP", "cpp"
  );
  private static final Pattern PROBLEM_MARKER = Pattern.compile("-- problem: ([a-z0-9-]+)");
  private static final Pattern STARTER = Pattern.compile(
      "'(PYTHON|JAVA|CPP)',\\s*\\$([a-zA-Z0-9_]+)\\$(.*?)\\$\\2\\$",
      Pattern.DOTALL
  );
  private static final ObjectMapper MAPPER = new ObjectMapper();

  @TempDir
  static Path tempDirectory;

  private static DockerCodeExecutor executor;

  @BeforeAll
  static void setUp() throws IOException {
    assumeTrue(dockerAvailable(), "Docker CLI/daemon is not available");
    assumeTrue(
        runnerImagesAvailable(),
        "Runner images are missing. Build them with: " + String.join(" && ", RunnerImages.all().stream()
            .map(RunnerImages::buildCommand)
            .toList())
    );

    Path workDirectory = tempDirectory.resolve("runs");
    Files.createDirectories(workDirectory);
    executor = new DockerCodeExecutor(true, "docker", workDirectory.toString());
  }

  @Test
  void migrationHasRunnableStarterForEveryProblemAndLanguage() throws IOException {
    var problems = loadProblems();
    var starters = loadStarters();

    assertEquals(15, problems.size(), "cases.json must cover all seeded problems");
    assertEquals(15, starters.size(), "V7 must define starter code for all seeded problems");
    for (JsonNode problem : problems) {
      String slug = problem.get("slug").asText();
      var byLanguage = starters.get(slug);
      assertNotNull(byLanguage, () -> "V7 is missing a starter block for " + slug);
      assertEquals(Set.of("PYTHON", "JAVA", "CPP"), byLanguage.keySet(), () -> slug + " has incomplete starter code");
    }
  }

  @Test
  void everyReferenceSolutionScores100() throws IOException {
    for (JsonNode problem : loadProblems()) {
      String slug = problem.get("slug").asText();
      List<Case> cases = readCases(problem);
      for (String language : LANGUAGES) {
        String source = readReference(slug, language);
        int passed = run(language, source, cases, false);
        assertEquals(
            cases.size(),
            passed,
            () -> slug + " / " + language + " reference passed " + passed + "/" + cases.size()
        );
      }
    }
  }

  @Test
  void everyUntouchedStarterScoresBelow100() throws IOException {
    var starters = loadStarters();
    for (JsonNode problem : loadProblems()) {
      String slug = problem.get("slug").asText();
      List<Case> cases = readCases(problem);
      for (String language : LANGUAGES) {
        String source = starters.get(slug).get(language);
        int passed = run(language, source, cases, true);
        assertTrue(
            passed < cases.size(),
            () -> slug + " / " + language + " untouched starter passed " + passed + "/" + cases.size()
        );
      }
    }
  }

  private static int run(String language, String source, List<Case> cases, boolean stopOnFailure) {
    try (PreparedExecution execution = executor.prepare(language, source)) {
      CompileResult compile = execution.compile();
      assertTrue(compile.success(), () -> language + " code did not compile:\n" + compile.message());

      int passed = 0;
      for (Case test : cases) {
        ExecutionResult result = execution.run(test.input);
        boolean matches = result.status() == SubmissionStatus.SUCCESS
            && normalize(result.stdout()).equals(normalize(test.expectedOutput));
        if (matches) {
          passed++;
        } else if (stopOnFailure) {
          break;
        }
      }
      return passed;
    }
  }

  private static JsonNode loadProblems() throws IOException {
    try (InputStream stream = ReferenceSolutionTest.class.getResourceAsStream("/reference-solutions/cases.json")) {
      assertNotNull(stream, "reference-solutions/cases.json is missing");
      return MAPPER.readTree(stream);
    }
  }

  private static Map<String, Map<String, String>> loadStarters() throws IOException {
    String sql = readResource("/db/migration/V7__runnable_starter_code.sql");

    Matcher marker = PROBLEM_MARKER.matcher(sql);
    List<Integer> starts = new ArrayList<>();
    List<String> slugs = new ArrayList<>();
    while (marker.find()) {
      starts.add(marker.start());
      slugs.add(marker.group(1));
    }

    var starters = new LinkedHashMap<String, Map<String, String>>();
    for (int i = 0; i < starts.size(); i++) {
      int end = i + 1 < starts.size() ? starts.get(i + 1) : sql.length();
      String chunk = sql.substring(starts.get(i), end);
      Matcher starter = STARTER.matcher(chunk);
      var byLanguage = new HashMap<String, String>();
      while (starter.find()) {
        byLanguage.put(starter.group(1), starter.group(3));
      }
      starters.put(slugs.get(i), byLanguage);
    }
    return starters;
  }

  private static List<Case> readCases(JsonNode problem) {
    List<Case> cases = new ArrayList<>();
    for (JsonNode test : problem.get("cases")) {
      cases.add(new Case(test.get("input").asText(), test.get("expectedOutput").asText()));
    }
    return cases;
  }

  private static String readReference(String slug, String language) throws IOException {
    return readResource("/reference-solutions/" + slug + "." + EXTENSIONS.get(language));
  }

  private static String readResource(String path) throws IOException {
    try (InputStream stream = ReferenceSolutionTest.class.getResourceAsStream(path)) {
      assertNotNull(stream, path + " is missing");
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static String normalize(String value) {
    return value == null ? "" : value.replace("\r\n", "\n").trim();
  }

  private static boolean dockerAvailable() {
    try {
      Process process = new ProcessBuilder(
          "docker", "version", "--format", "{{.Server.Version}}"
      ).redirectErrorStream(true).start();
      process.getInputStream().readAllBytes();
      return process.waitFor(15, TimeUnit.SECONDS) && process.exitValue() == 0;
    } catch (IOException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private static boolean runnerImagesAvailable() {
    try {
      for (String image : RunnerImages.all()) {
        Process process = new ProcessBuilder(
            "docker", "image", "inspect", "--format", "{{.Id}}", image
        ).redirectErrorStream(true).start();
        process.getInputStream().readAllBytes();
        if (!process.waitFor(15, TimeUnit.SECONDS) || process.exitValue() != 0) {
          return false;
        }
      }
      return true;
    } catch (IOException e) {
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  private record Case(String input, String expectedOutput) {}
}
