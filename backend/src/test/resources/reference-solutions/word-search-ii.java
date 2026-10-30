import java.util.*;

public class Main {
  static List<String> findWords(char[][] board, String[] words) {
    List<String> found = new ArrayList<>();
    for (String word : words) {
      if (exists(board, word)) found.add(word);
    }
    return found;
  }

  static boolean exists(char[][] board, String word) {
    for (int row = 0; row < board.length; row++) {
      for (int col = 0; col < board[row].length; col++) {
        if (board[row][col] == word.charAt(0) && search(board, word, row, col, 0)) {
          return true;
        }
      }
    }
    return false;
  }

  static boolean search(char[][] board, String word, int row, int col, int index) {
    if (index == word.length()) return true;
    if (row < 0 || row >= board.length || col < 0 || col >= board[row].length) return false;
    if (board[row][col] != word.charAt(index)) return false;
    char saved = board[row][col];
    board[row][col] = '#';
    boolean found = search(board, word, row + 1, col, index + 1)
        || search(board, word, row - 1, col, index + 1)
        || search(board, word, row, col + 1, index + 1)
        || search(board, word, row, col - 1, index + 1);
    board[row][col] = saved;
    return found;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    List<char[]> boardRows = new ArrayList<>();
    List<String> words = new ArrayList<>();
    boolean readingBoard = true;
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      if (line.isEmpty()) continue;
      String[] tokens = line.split("\\s+");
      boolean isBoardRow = readingBoard;
      if (isBoardRow) {
        for (String token : tokens) {
          if (token.length() != 1) {
            isBoardRow = false;
            break;
          }
        }
      }
      if (isBoardRow) {
        char[] cells = new char[tokens.length];
        for (int i = 0; i < tokens.length; i++) cells[i] = tokens[i].charAt(0);
        boardRows.add(cells);
      } else {
        readingBoard = false;
        words.add(line);
      }
    }
    char[][] board = boardRows.toArray(new char[0][]);
    List<String> found = findWords(board, words.toArray(new String[0]));
    Collections.sort(found);
    for (String word : found) System.out.println(word);
  }
}
