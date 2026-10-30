import java.util.*;

public class Main {
  static String minWindow(String s, String t) {
    if (t.isEmpty()) return "";
    Map<Character, Integer> need = new HashMap<>();
    for (char c : t.toCharArray()) need.merge(c, 1, Integer::sum);
    int missing = t.length();
    int left = 0;
    int bestStart = 0;
    int bestLength = Integer.MAX_VALUE;
    for (int right = 0; right < s.length(); right++) {
      char c = s.charAt(right);
      int remaining = need.getOrDefault(c, 0);
      if (remaining > 0) missing--;
      need.put(c, remaining - 1);
      while (missing == 0) {
        if (right - left + 1 < bestLength) {
          bestStart = left;
          bestLength = right - left + 1;
        }
        char removed = s.charAt(left);
        int count = need.get(removed) + 1;
        need.put(removed, count);
        if (count > 0) missing++;
        left++;
      }
    }
    return bestLength == Integer.MAX_VALUE ? "" : s.substring(bestStart, bestStart + bestLength);
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String s = scanner.hasNextLine() ? scanner.nextLine() : "";
    String t = scanner.hasNextLine() ? scanner.nextLine() : "";
    System.out.println(minWindow(s, t));
  }
}
