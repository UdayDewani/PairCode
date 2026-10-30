import java.util.*;

public class Main {
  static List<int[]> merge(List<int[]> intervals) {
    List<int[]> answer = new ArrayList<>();
    if (intervals.isEmpty()) return answer;
    List<int[]> ordered = new ArrayList<>(intervals);
    ordered.sort(Comparator.comparingInt(interval -> interval[0]));
    int[] current = new int[]{ordered.get(0)[0], ordered.get(0)[1]};
    for (int i = 1; i < ordered.size(); i++) {
      int[] interval = ordered.get(i);
      if (interval[0] <= current[1]) {
        current[1] = Math.max(current[1], interval[1]);
      } else {
        answer.add(current);
        current = new int[]{interval[0], interval[1]};
      }
    }
    answer.add(current);
    return answer;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    List<int[]> intervals = new ArrayList<>();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      if (line.isEmpty()) continue;
      String[] tokens = line.split("\\s+");
      intervals.add(new int[]{Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1])});
    }
    for (int[] interval : merge(intervals)) {
      System.out.println(interval[0] + " " + interval[1]);
    }
  }
}
