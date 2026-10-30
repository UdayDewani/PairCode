import java.util.*;

public class Main {
  static int lengthOfLongestSubstring(String s) {
    Map<Character, Integer> lastSeen = new HashMap<>();
    int left = 0;
    int best = 0;
    for (int right = 0; right < s.length(); right++) {
      char c = s.charAt(right);
      if (lastSeen.containsKey(c) && lastSeen.get(c) >= left) {
        left = lastSeen.get(c) + 1;
      }
      lastSeen.put(c, right);
      best = Math.max(best, right - left + 1);
    }
    return best;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String s = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    System.out.println(lengthOfLongestSubstring(s));
  }
}
