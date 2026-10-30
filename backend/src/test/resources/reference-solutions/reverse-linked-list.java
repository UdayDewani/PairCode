import java.util.*;

public class Main {
  static int[] reverseList(int[] values) {
    int[] answer = new int[values.length];
    for (int i = 0; i < values.length; i++) {
      answer[i] = values[values.length - 1 - i];
    }
    return answer;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    String[] tokens = line.isEmpty() ? new String[0] : line.split("\\s+");
    int[] values = new int[tokens.length];
    for (int i = 0; i < tokens.length; i++) values[i] = Integer.parseInt(tokens[i]);
    int[] answer = reverseList(values);
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < answer.length; i++) {
      if (i > 0) output.append(' ');
      output.append(answer[i]);
    }
    System.out.println(output);
  }
}
