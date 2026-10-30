import java.util.*;

public class Main {
  static boolean isValid(String s) {
    Deque<Character> stack = new ArrayDeque<>();
    for (char c : s.toCharArray()) {
      if (c == '(' || c == '[' || c == '{') {
        stack.push(c);
      } else {
        if (stack.isEmpty()) return false;
        char open = stack.pop();
        if ((c == ')' && open != '(') || (c == ']' && open != '[') || (c == '}' && open != '{')) {
          return false;
        }
      }
    }
    return stack.isEmpty();
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String s = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    System.out.println(isValid(s) ? "true" : "false");
  }
}
