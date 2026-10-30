import java.util.*;

public class Main {
  static int climbStairs(int n) {
    int previous = 1;
    int current = 1;
    for (int i = 0; i < n; i++) {
      int next = previous + current;
      previous = current;
      current = next;
    }
    return previous;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    System.out.println(climbStairs(Integer.parseInt(scanner.nextLine().trim())));
  }
}
