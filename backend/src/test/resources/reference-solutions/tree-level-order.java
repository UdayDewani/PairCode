import java.util.*;

public class Main {
  static class TreeNode {
    int value;
    TreeNode left;
    TreeNode right;

    TreeNode(int value) {
      this.value = value;
    }
  }

  static TreeNode buildTree(String[] tokens) {
    if (tokens.length == 0 || tokens[0].equals("null")) return null;
    TreeNode root = new TreeNode(Integer.parseInt(tokens[0]));
    Queue<TreeNode> queue = new LinkedList<>();
    queue.add(root);
    int index = 1;
    while (!queue.isEmpty() && index < tokens.length) {
      TreeNode node = queue.remove();
      if (index < tokens.length && !tokens[index].equals("null")) {
        node.left = new TreeNode(Integer.parseInt(tokens[index]));
        queue.add(node.left);
      }
      index++;
      if (index < tokens.length && !tokens[index].equals("null")) {
        node.right = new TreeNode(Integer.parseInt(tokens[index]));
        queue.add(node.right);
      }
      index++;
    }
    return root;
  }

  static List<List<Integer>> levelOrder(TreeNode root) {
    List<List<Integer>> levels = new ArrayList<>();
    if (root == null) return levels;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.add(root);
    while (!queue.isEmpty()) {
      int size = queue.size();
      List<Integer> level = new ArrayList<>();
      for (int i = 0; i < size; i++) {
        TreeNode node = queue.remove();
        level.add(node.value);
        if (node.left != null) queue.add(node.left);
        if (node.right != null) queue.add(node.right);
      }
      levels.add(level);
    }
    return levels;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    String[] tokens = line.isEmpty() ? new String[0] : line.split("\\s+");
    TreeNode root = buildTree(tokens);
    for (List<Integer> level : levelOrder(root)) {
      StringJoiner output = new StringJoiner(" ");
      for (int value : level) output.add(String.valueOf(value));
      System.out.println(output);
    }
  }
}
