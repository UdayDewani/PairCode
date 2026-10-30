import java.util.*;

public class Main {
  static List<Integer> mergeKLists(List<List<Integer>> lists) {
    List<Integer> merged = new ArrayList<>();
    for (List<Integer> values : lists) merged.addAll(values);
    Collections.sort(merged);
    return merged;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    List<List<Integer>> lists = new ArrayList<>();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      List<Integer> values = new ArrayList<>();
      if (!line.isEmpty()) {
        for (String token : line.split("\\s+")) values.add(Integer.parseInt(token));
      }
      lists.add(values);
    }
    List<Integer> merged = mergeKLists(lists);
    StringJoiner output = new StringJoiner(" ");
    for (int value : merged) output.add(String.valueOf(value));
    System.out.println(output);
  }
}
