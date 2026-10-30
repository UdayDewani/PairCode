import java.util.*;

public class Main {
  static int[] twoSum(int[] nums, int target) {
    Map<Integer, Integer> seen = new HashMap<>();
    for (int i = 0; i < nums.length; i++) {
      int needed = target - nums[i];
      if (seen.containsKey(needed)) return new int[]{seen.get(needed), i};
      seen.put(nums[i], i);
    }
    return new int[0];
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String[] first = scanner.nextLine().trim().split("\\s+");
    int[] nums = new int[first.length];
    for (int i = 0; i < first.length; i++) nums[i] = Integer.parseInt(first[i]);
    int target = Integer.parseInt(scanner.nextLine().trim());
    int[] answer = twoSum(nums, target);
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < answer.length; i++) {
      if (i > 0) output.append(' ');
      output.append(answer[i]);
    }
    System.out.println(output);
  }
}
