import java.util.*;

public class Main {
  static int numIslands(char[][] grid) {
    if (grid.length == 0 || grid[0].length == 0) return 0;
    int rows = grid.length;
    int cols = grid[0].length;
    boolean[][] visited = new boolean[rows][cols];
    int count = 0;
    int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < cols; col++) {
        if (grid[row][col] != '1' || visited[row][col]) continue;
        count++;
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{row, col});
        visited[row][col] = true;
        while (!stack.isEmpty()) {
          int[] current = stack.pop();
          for (int[] direction : directions) {
            int nextRow = current[0] + direction[0];
            int nextCol = current[1] + direction[1];
            if (nextRow >= 0 && nextRow < rows && nextCol >= 0 && nextCol < cols
                && grid[nextRow][nextCol] == '1' && !visited[nextRow][nextCol]) {
              visited[nextRow][nextCol] = true;
              stack.push(new int[]{nextRow, nextCol});
            }
          }
        }
      }
    }
    return count;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    List<char[]> rows = new ArrayList<>();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      if (line.isEmpty()) continue;
      String[] tokens = line.split("\\s+");
      char[] row = new char[tokens.length];
      for (int i = 0; i < tokens.length; i++) row[i] = tokens[i].charAt(0);
      rows.add(row);
    }
    System.out.println(numIslands(rows.toArray(new char[0][])));
  }
}
