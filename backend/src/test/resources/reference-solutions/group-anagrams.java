import java.util.*;

public class Main {
  static List<List<String>> groupAnagrams(String[] words) {
    Map<String, List<String>> groups = new LinkedHashMap<>();
    for (String word : words) {
      char[] letters = word.toCharArray();
      Arrays.sort(letters);
      String key = new String(letters);
      groups.computeIfAbsent(key, ignored -> new ArrayList<>()).add(word);
    }
    return new ArrayList<>(groups.values());
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    String[] words = line.isEmpty() ? new String[0] : line.split("\\s+");
    List<List<String>> groups = groupAnagrams(words);
    List<String> lines = new ArrayList<>();
    for (List<String> group : groups) {
      if (group.isEmpty()) continue;
      List<String> copy = new ArrayList<>(group);
      Collections.sort(copy);
      lines.add(String.join(" ", copy));
    }
    Collections.sort(lines);
    for (String output : lines) System.out.println(output);
  }
}
