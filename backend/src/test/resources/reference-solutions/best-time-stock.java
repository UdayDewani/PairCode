import java.util.*;

public class Main {
  static int maxProfit(int[] prices) {
    int lowest = Integer.MAX_VALUE;
    int best = 0;
    for (int price : prices) {
      lowest = Math.min(lowest, price);
      best = Math.max(best, price - lowest);
    }
    return best;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String line = scanner.nextLine().trim();
    String[] tokens = line.isEmpty() ? new String[0] : line.split("\\s+");
    int[] prices = new int[tokens.length];
    for (int i = 0; i < tokens.length; i++) prices[i] = Integer.parseInt(tokens[i]);
    System.out.println(maxProfit(prices));
  }
}
