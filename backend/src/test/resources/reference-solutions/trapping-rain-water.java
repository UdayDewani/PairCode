import java.util.*;

public class Main {
  static int trap(int[] height) {
    if (height.length == 0) return 0;
    int left = 0;
    int right = height.length - 1;
    int leftMax = 0;
    int rightMax = 0;
    int water = 0;
    while (left < right) {
      if (height[left] < height[right]) {
        leftMax = Math.max(leftMax, height[left]);
        water += leftMax - height[left];
        left++;
      } else {
        rightMax = Math.max(rightMax, height[right]);
        water += rightMax - height[right];
        right--;
      }
    }
    return water;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String line = scanner.nextLine().trim();
    String[] tokens = line.isEmpty() ? new String[0] : line.split("\\s+");
    int[] height = new int[tokens.length];
    for (int i = 0; i < tokens.length; i++) height[i] = Integer.parseInt(tokens[i]);
    System.out.println(trap(height));
  }
}
