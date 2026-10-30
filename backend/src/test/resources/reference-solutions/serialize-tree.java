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

  static class Codec {
    String serialize(TreeNode root) {
      if (root == null) return "";
      List<String> tokens = new ArrayList<>();
      Queue<TreeNode> queue = new LinkedList<>();
      queue.add(root);
      while (!queue.isEmpty()) {
        TreeNode node = queue.remove();
        if (node == null) {
          tokens.add("null");
          continue;
        }
        tokens.add(String.valueOf(node.value));
        queue.add(node.left);
        queue.add(node.right);
      }
      while (!tokens.isEmpty() && tokens.get(tokens.size() - 1).equals("null")) {
        tokens.remove(tokens.size() - 1);
      }
      return String.join(" ", tokens);
    }

    TreeNode deserialize(String data) {
      String[] tokens = data.trim().isEmpty() ? new String[0] : data.trim().split("\\s+");
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

  static List<String> treeTokens(TreeNode root) {
    List<String> tokens = new ArrayList<>();
    if (root == null) return tokens;
    Queue<TreeNode> queue = new LinkedList<>();
    queue.add(root);
    while (!queue.isEmpty()) {
      TreeNode node = queue.remove();
      if (node == null) {
        tokens.add("null");
        continue;
      }
      tokens.add(String.valueOf(node.value));
      queue.add(node.left);
      queue.add(node.right);
    }
    while (!tokens.isEmpty() && tokens.get(tokens.size() - 1).equals("null")) {
      tokens.remove(tokens.size() - 1);
    }
    return tokens;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    String[] tokens = line.isEmpty() ? new String[0] : line.split("\\s+");
    TreeNode root = buildTree(tokens);
    Codec codec = new Codec();
    TreeNode restored = codec.deserialize(codec.serialize(root));
    System.out.println(String.join(" ", treeTokens(restored)));
  }
}
