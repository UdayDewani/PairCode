-- V7: Runnable starter programs and explicit stdin/stdout contracts.
--
-- The judge compiles and runs whole programs against stdin/stdout test cases, so every starter
-- must read the problem's exact input format, call a clearly marked function that the candidate
-- completes, and print the expected format. The untouched starter intentionally produces wrong
-- output for at least one test in every problem.

-- problem: two-sum
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def two_sum(nums, target):
    # TODO: return the indices of the two numbers that add up to target.
    return []


def main():
    lines = sys.stdin.read().splitlines()
    if len(lines) < 2:
        return
    nums = [int(value) for value in lines[0].split()]
    target = int(lines[1])
    answer = two_sum(nums, target)
    print(" ".join(str(index) for index in answer))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static int[] twoSum(int[] nums, int target) {
    // TODO: return the indices of the two numbers that add up to target.
    return new int[0];
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String[] first = scanner.nextLine().trim().split("\\s+");
    int[] nums = new int[first.length];
    for (int i = 0; i < first.length; i++) {
      nums[i] = Integer.parseInt(first[i]);
    }
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
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

vector<int> twoSum(vector<int>& nums, int target) {
    // TODO: return the indices of the two numbers that add up to target.
    return {};
}

int main() {
    string line;
    getline(cin, line);
    istringstream first(line);
    vector<int> nums;
    int value;
    while (first >> value) nums.push_back(value);
    int target;
    cin >> target;
    vector<int> answer = twoSum(nums, target);
    for (size_t i = 0; i < answer.size(); ++i) {
        if (i) cout << ' ';
        cout << answer[i];
    }
    cout << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: nums, space-separated integers.\nLine 2: target integer.\nOutput format:\nThe two indices in ascending order, separated by a single space.'
WHERE id = '00000000-0000-0000-0000-000000000001';

-- problem: valid-parentheses
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def is_valid(s):
    # TODO: return True when every bracket is closed in the correct order.
    return False


def main():
    s = sys.stdin.read().strip()
    print("true" if is_valid(s) else "false")


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static boolean isValid(String s) {
    // TODO: return true when every bracket is closed in the correct order.
    return false;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String s = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    System.out.println(isValid(s) ? "true" : "false");
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

bool isValid(const string& s) {
    // TODO: return true when every bracket is closed in the correct order.
    return false;
}

int main() {
    string s;
    getline(cin, s);
    cout << (isValid(s) ? "true" : "false") << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: the bracket string s.\nOutput format:\nPrint true if the string is valid, otherwise false.'
WHERE id = '00000000-0000-0000-0000-000000000002';

-- problem: best-time-stock
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def max_profit(prices):
    # TODO: return the best profit from one buy and one later sell.
    return 0


def main():
    prices = [int(value) for value in sys.stdin.read().split()]
    print(max_profit(prices))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static int maxProfit(int[] prices) {
    // TODO: return the best profit from one buy and one later sell.
    return 0;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String line = scanner.nextLine().trim();
    String[] tokens = line.isEmpty() ? new String[0] : line.split("\\s+");
    int[] prices = new int[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      prices[i] = Integer.parseInt(tokens[i]);
    }
    System.out.println(maxProfit(prices));
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

int maxProfit(const vector<int>& prices) {
    // TODO: return the best profit from one buy and one later sell.
    return 0;
}

int main() {
    vector<int> prices;
    int value;
    while (cin >> value) prices.push_back(value);
    cout << maxProfit(prices) << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: prices, space-separated integers.\nOutput format:\nA single integer: the maximum profit. Print 0 when no profit is possible.'
WHERE id = '00000000-0000-0000-0000-000000000003';

-- problem: reverse-linked-list
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def reverse_list(values):
    # TODO: return the values in reverse order.
    return []


def main():
    values = [int(value) for value in sys.stdin.read().split()]
    answer = reverse_list(values)
    print(" ".join(str(value) for value in answer))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static int[] reverseList(int[] values) {
    // TODO: return the values in reverse order.
    return new int[0];
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    String[] tokens = line.isEmpty() ? new String[0] : line.split("\\s+");
    int[] values = new int[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      values[i] = Integer.parseInt(tokens[i]);
    }
    int[] answer = reverseList(values);
    StringBuilder output = new StringBuilder();
    for (int i = 0; i < answer.length; i++) {
      if (i > 0) output.append(' ');
      output.append(answer[i]);
    }
    System.out.println(output);
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

vector<int> reverseList(const vector<int>& values) {
    // TODO: return the values in reverse order.
    return {};
}

int main() {
    vector<int> values;
    int value;
    while (cin >> value) values.push_back(value);
    vector<int> answer = reverseList(values);
    for (size_t i = 0; i < answer.size(); ++i) {
        if (i) cout << ' ';
        cout << answer[i];
    }
    cout << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: the linked-list values in order, space-separated.\nOutput format:\nThe values in reverse order, space-separated.'
WHERE id = '00000000-0000-0000-0000-000000000004';

-- problem: climbing-stairs
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def climb_stairs(n):
    # TODO: return the number of distinct ways to climb n stairs.
    return 0


def main():
    n = int(sys.stdin.read().strip())
    print(climb_stairs(n))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static int climbStairs(int n) {
    // TODO: return the number of distinct ways to climb n stairs.
    return 0;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    int n = Integer.parseInt(scanner.nextLine().trim());
    System.out.println(climbStairs(n));
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

int climbStairs(int n) {
    // TODO: return the number of distinct ways to climb n stairs.
    return 0;
}

int main() {
    int n;
    cin >> n;
    cout << climbStairs(n) << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: the integer n.\nOutput format:\nA single integer: the number of distinct ways to reach the top.'
WHERE id = '00000000-0000-0000-0000-000000000005';

-- problem: longest-substring
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def length_of_longest_substring(s):
    # TODO: return the length of the longest substring without repeating characters.
    return 0


def main():
    s = sys.stdin.read().strip()
    print(length_of_longest_substring(s))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static int lengthOfLongestSubstring(String s) {
    // TODO: return the length of the longest substring without repeating characters.
    return 0;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String s = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    System.out.println(lengthOfLongestSubstring(s));
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

int lengthOfLongestSubstring(const string& s) {
    // TODO: return the length of the longest substring without repeating characters.
    return 0;
}

int main() {
    string s;
    getline(cin, s);
    cout << lengthOfLongestSubstring(s) << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: the string s (lowercase letters).\nOutput format:\nA single integer: the length of the longest substring without repeated characters.'
WHERE id = '00000000-0000-0000-0000-000000000006';

-- problem: group-anagrams
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def group_anagrams(words):
    # TODO: return a list of anagram groups.
    return []


def main():
    words = sys.stdin.read().split()
    groups = group_anagrams(words)
    lines = [" ".join(sorted(group)) for group in groups if group]
    for line in sorted(lines):
        print(line)


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static List<List<String>> groupAnagrams(String[] words) {
    // TODO: return a list of anagram groups.
    return new ArrayList<>();
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String line = scanner.hasNextLine() ? scanner.nextLine().trim() : "";
    String[] words = line.isEmpty() ? new String[0] : line.split("\\s+");
    List<List<String>> groups = groupAnagrams(words);
    List<String> lines = new ArrayList<>();
    for (List<String> group : groups) {
      if (group.isEmpty()) continue;
      List<String> copy = new ArrayList<>(group);
      Collections.sort(copy);
      lines.add(String.join(" ", copy));
    }
    Collections.sort(lines);
    for (String output : lines) {
      System.out.println(output);
    }
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

vector<vector<string>> groupAnagrams(const vector<string>& words) {
    // TODO: return a list of anagram groups.
    return {};
}

int main() {
    string line;
    getline(cin, line);
    istringstream input(line);
    vector<string> words;
    string word;
    while (input >> word) words.push_back(word);
    vector<vector<string>> groups = groupAnagrams(words);
    vector<string> lines;
    for (vector<string> group : groups) {
        if (group.empty()) continue;
        sort(group.begin(), group.end());
        string joined;
        for (size_t i = 0; i < group.size(); ++i) {
            if (i) joined += ' ';
            joined += group[i];
        }
        lines.push_back(joined);
    }
    sort(lines.begin(), lines.end());
    for (const string& output : lines) cout << output << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: the words, space-separated.\nOutput format:\nPrint one anagram group per line. Sort the words inside each group alphabetically, join them with single spaces, and print the lines in alphabetical order.'
WHERE id = '00000000-0000-0000-0000-000000000007';

-- problem: number-of-islands
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def num_islands(grid):
    # TODO: return the number of connected groups of 1 cells.
    return 0


def main():
    grid = [line.split() for line in sys.stdin.read().splitlines() if line.strip()]
    print(num_islands(grid))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static int numIslands(char[][] grid) {
    // TODO: return the number of connected groups of 1 cells.
    return 0;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    List<char[]> rows = new ArrayList<>();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      if (line.isEmpty()) continue;
      String[] tokens = line.split("\\s+");
      char[] row = new char[tokens.length];
      for (int i = 0; i < tokens.length; i++) {
        row[i] = tokens[i].charAt(0);
      }
      rows.add(row);
    }
    System.out.println(numIslands(rows.toArray(new char[0][])));
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

int numIslands(vector<vector<char>>& grid) {
    // TODO: return the number of connected groups of 1 cells.
    return 0;
}

int main() {
    vector<vector<char>> grid;
    string line;
    while (getline(cin, line)) {
        istringstream input(line);
        vector<char> row;
        string cell;
        while (input >> cell) row.push_back(cell[0]);
        if (!row.empty()) grid.push_back(row);
    }
    cout << numIslands(grid) << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nOne grid row per line until EOF. Each row contains 0 and 1 cells separated by spaces. All rows have the same number of columns.\nOutput format:\nA single integer: the number of islands.'
WHERE id = '00000000-0000-0000-0000-000000000008';

-- problem: tree-level-order
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


class TreeNode:
    def __init__(self, value=0, left=None, right=None):
        self.value = value
        self.left = left
        self.right = right


def build_tree(tokens):
    if not tokens or tokens[0] == "null":
        return None
    root = TreeNode(int(tokens[0]))
    queue = [root]
    index = 1
    while queue and index < len(tokens):
        node = queue.pop(0)
        if index < len(tokens) and tokens[index] != "null":
            node.left = TreeNode(int(tokens[index]))
            queue.append(node.left)
        index += 1
        if index < len(tokens) and tokens[index] != "null":
            node.right = TreeNode(int(tokens[index]))
            queue.append(node.right)
        index += 1
    return root


def level_order(root):
    # TODO: return the node values grouped by level from top to bottom.
    return []


def main():
    tokens = sys.stdin.read().split()
    root = build_tree(tokens)
    for level in level_order(root):
        print(" ".join(str(value) for value in level))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  static class TreeNode {
    int value;
    TreeNode left;
    TreeNode right;

    TreeNode(int value) {
      this.value = value;
    }
  }

  public static TreeNode buildTree(String[] tokens) {
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

  public static List<List<Integer>> levelOrder(TreeNode root) {
    // TODO: return the node values grouped by level from top to bottom.
    return new ArrayList<>();
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
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

struct TreeNode {
    int value;
    TreeNode* left;
    TreeNode* right;
    TreeNode(int value) : value(value), left(nullptr), right(nullptr) {}
};

TreeNode* buildTree(const vector<string>& tokens) {
    if (tokens.empty() || tokens[0] == "null") return nullptr;
    TreeNode* root = new TreeNode(stoi(tokens[0]));
    queue<TreeNode*> pending;
    pending.push(root);
    size_t index = 1;
    while (!pending.empty() && index < tokens.size()) {
        TreeNode* node = pending.front();
        pending.pop();
        if (index < tokens.size() && tokens[index] != "null") {
            node->left = new TreeNode(stoi(tokens[index]));
            pending.push(node->left);
        }
        index++;
        if (index < tokens.size() && tokens[index] != "null") {
            node->right = new TreeNode(stoi(tokens[index]));
            pending.push(node->right);
        }
        index++;
    }
    return root;
}

vector<vector<int>> levelOrder(TreeNode* root) {
    // TODO: return the node values grouped by level from top to bottom.
    return {};
}

int main() {
    string line;
    getline(cin, line);
    istringstream input(line);
    vector<string> tokens;
    string token;
    while (input >> token) tokens.push_back(token);
    TreeNode* root = buildTree(tokens);
    for (const vector<int>& level : levelOrder(root)) {
        for (size_t i = 0; i < level.size(); ++i) {
            if (i) cout << ' ';
            cout << level[i];
        }
        cout << '\n';
    }
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: the tree in breadth-first order, space-separated. Use null for a missing child.\nOutput format:\nOne line per level, with the level values separated by spaces.'
WHERE id = '00000000-0000-0000-0000-000000000009';

-- problem: merge-intervals
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def merge(intervals):
    # TODO: return the merged intervals.
    return []


def main():
    intervals = []
    for line in sys.stdin.read().splitlines():
        if line.strip():
            start, end = (int(value) for value in line.split())
            intervals.append([start, end])
    for start, end in merge(intervals):
        print(start, end)


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static List<int[]> merge(List<int[]> intervals) {
    // TODO: return the merged intervals.
    return new ArrayList<>();
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    List<int[]> intervals = new ArrayList<>();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      if (line.isEmpty()) continue;
      String[] tokens = line.split("\\s+");
      intervals.add(new int[]{Integer.parseInt(tokens[0]), Integer.parseInt(tokens[1])});
    }
    for (int[] interval : merge(intervals)) {
      System.out.println(interval[0] + " " + interval[1]);
    }
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

vector<vector<int>> merge(vector<vector<int>>& intervals) {
    // TODO: return the merged intervals.
    return {};
}

int main() {
    vector<vector<int>> intervals;
    string line;
    while (getline(cin, line)) {
        istringstream input(line);
        int start, end;
        if (input >> start >> end) intervals.push_back({start, end});
    }
    vector<vector<int>> answer = merge(intervals);
    for (const vector<int>& interval : answer) {
        cout << interval[0] << ' ' << interval[1] << '\n';
    }
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nEach line until EOF contains two integers: start end.\nOutput format:\nOne merged interval per line as "start end", sorted by start.'
WHERE id = '00000000-0000-0000-0000-000000000010';

-- problem: merge-k-lists
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def merge_k_lists(lists):
    # TODO: return all values from the sorted lists in one sorted list.
    return []


def main():
    lines = sys.stdin.read().splitlines()
    lists = [[int(value) for value in line.split()] for line in lines]
    merged = merge_k_lists(lists)
    print(" ".join(str(value) for value in merged))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static List<Integer> mergeKLists(List<List<Integer>> lists) {
    // TODO: return all values from the sorted lists in one sorted list.
    return new ArrayList<>();
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    List<List<Integer>> lists = new ArrayList<>();
    while (scanner.hasNextLine()) {
      String line = scanner.nextLine().trim();
      List<Integer> values = new ArrayList<>();
      if (!line.isEmpty()) {
        for (String token : line.split("\\s+")) {
          values.add(Integer.parseInt(token));
        }
      }
      lists.add(values);
    }
    List<Integer> merged = mergeKLists(lists);
    StringJoiner output = new StringJoiner(" ");
    for (int value : merged) output.add(String.valueOf(value));
    System.out.println(output);
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

vector<int> mergeKLists(const vector<vector<int>>& lists) {
    // TODO: return all values from the sorted lists in one sorted list.
    return {};
}

int main() {
    vector<vector<int>> lists;
    string line;
    while (getline(cin, line)) {
        istringstream input(line);
        vector<int> values;
        int value;
        while (input >> value) values.push_back(value);
        lists.push_back(values);
    }
    vector<int> merged = mergeKLists(lists);
    for (size_t i = 0; i < merged.size(); ++i) {
        if (i) cout << ' ';
        cout << merged[i];
    }
    cout << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nOne sorted list per line until EOF. Each list contains space-separated integers; an empty line means an empty list.\nOutput format:\nAll values merged into one sorted line, separated by spaces. Print a blank line when the result is empty.'
WHERE id = '00000000-0000-0000-0000-000000000011';

-- problem: trapping-rain-water
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def trap(height):
    # TODO: return the total amount of trapped rain water.
    return 0


def main():
    height = [int(value) for value in sys.stdin.read().split()]
    print(trap(height))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static int trap(int[] height) {
    // TODO: return the total amount of trapped rain water.
    return 0;
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String line = scanner.nextLine().trim();
    String[] tokens = line.isEmpty() ? new String[0] : line.split("\\s+");
    int[] height = new int[tokens.length];
    for (int i = 0; i < tokens.length; i++) {
      height[i] = Integer.parseInt(tokens[i]);
    }
    System.out.println(trap(height));
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

int trap(const vector<int>& height) {
    // TODO: return the total amount of trapped rain water.
    return 0;
}

int main() {
    vector<int> height;
    int value;
    while (cin >> value) height.push_back(value);
    cout << trap(height) << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: the elevation heights, space-separated integers.\nOutput format:\nA single integer: the total trapped rain water.'
WHERE id = '00000000-0000-0000-0000-000000000012';

-- problem: minimum-window
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def min_window(s, t):
    # TODO: return the smallest substring of s that contains every character of t.
    return ""


def main():
    lines = sys.stdin.read().splitlines()
    s = lines[0] if lines else ""
    t = lines[1] if len(lines) > 1 else ""
    print(min_window(s, t))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static String minWindow(String s, String t) {
    // TODO: return the smallest substring of s that contains every character of t.
    return "";
  }

  public static void main(String[] args) {
    Scanner scanner = new Scanner(System.in);
    String s = scanner.hasNextLine() ? scanner.nextLine() : "";
    String t = scanner.hasNextLine() ? scanner.nextLine() : "";
    System.out.println(minWindow(s, t));
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

string minWindow(const string& s, const string& t) {
    // TODO: return the smallest substring of s that contains every character of t.
    return "";
}

int main() {
    string s;
    string t;
    getline(cin, s);
    getline(cin, t);
    cout << minWindow(s, t) << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: the string s.\nLine 2: the string t.\nOutput format:\nThe minimum window substring, or a blank line when no such substring exists.'
WHERE id = '00000000-0000-0000-0000-000000000013';

-- problem: word-search-ii
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


def find_words(board, words):
    # TODO: return every word from words that appears in the board.
    return []


def main():
    lines = [line.strip() for line in sys.stdin.read().splitlines() if line.strip()]
    board = []
    words = []
    reading_board = True
    for line in lines:
        tokens = line.split()
        if reading_board and all(len(token) == 1 for token in tokens):
            board.append(tokens)
        else:
            reading_board = False
            words.append(line)
    for word in sorted(find_words(board, words)):
        print(word)


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  public static List<String> findWords(char[][] board, String[] words) {
    // TODO: return every word from words that appears in the board.
    return new ArrayList<>();
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
        for (int i = 0; i < tokens.length; i++) {
          cells[i] = tokens[i].charAt(0);
        }
        boardRows.add(cells);
      } else {
        readingBoard = false;
        words.add(line);
      }
    }
    char[][] board = boardRows.toArray(new char[0][]);
    List<String> found = findWords(board, words.toArray(new String[0]));
    Collections.sort(found);
    for (String word : found) {
      System.out.println(word);
    }
  }
}
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

vector<string> findWords(vector<vector<char>>& board, vector<string>& words) {
    // TODO: return every word from words that appears in the board.
    return {};
}

int main() {
    vector<vector<char>> board;
    vector<string> words;
    bool readingBoard = true;
    string line;
    while (getline(cin, line)) {
        if (line.empty()) continue;
        istringstream input(line);
        vector<string> tokens;
        string token;
        while (input >> token) tokens.push_back(token);
        bool isBoardRow = readingBoard;
        if (isBoardRow) {
            for (const string& item : tokens) {
                if (item.size() != 1) {
                    isBoardRow = false;
                    break;
                }
            }
        }
        if (isBoardRow) {
            vector<char> row;
            for (const string& item : tokens) row.push_back(item[0]);
            board.push_back(row);
        } else {
            readingBoard = false;
            words.push_back(line);
        }
    }
    vector<string> found = findWords(board, words);
    sort(found.begin(), found.end());
    for (const string& word : found) cout << word << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nFirst, the board rows. Each row contains single characters separated by spaces. Then the search words, one per line until EOF.\nOutput format:\nThe found words, one per line, in alphabetical order.'
WHERE id = '00000000-0000-0000-0000-000000000014';

-- problem: serialize-tree
UPDATE problems SET
  starter_code = jsonb_build_object(
    'PYTHON', $python$import sys


class TreeNode:
    def __init__(self, value=0, left=None, right=None):
        self.value = value
        self.left = left
        self.right = right


class Codec:
    def serialize(self, root):
        # TODO: return a string representation of the tree.
        return ""

    def deserialize(self, data):
        # TODO: rebuild and return the tree from serialize's output.
        return None


def build_tree(tokens):
    if not tokens or tokens[0] == "null":
        return None
    root = TreeNode(int(tokens[0]))
    queue = [root]
    index = 1
    while queue and index < len(tokens):
        node = queue.pop(0)
        if index < len(tokens) and tokens[index] != "null":
            node.left = TreeNode(int(tokens[index]))
            queue.append(node.left)
        index += 1
        if index < len(tokens) and tokens[index] != "null":
            node.right = TreeNode(int(tokens[index]))
            queue.append(node.right)
        index += 1
    return root


def tree_tokens(root):
    if root is None:
        return []
    tokens = []
    queue = [root]
    while queue:
        node = queue.pop(0)
        if node is None:
            tokens.append("null")
            continue
        tokens.append(str(node.value))
        queue.append(node.left)
        queue.append(node.right)
    while tokens and tokens[-1] == "null":
        tokens.pop()
    return tokens


def main():
    tokens = sys.stdin.read().split()
    root = build_tree(tokens)
    codec = Codec()
    restored = codec.deserialize(codec.serialize(root))
    print(" ".join(tree_tokens(restored)))


if __name__ == "__main__":
    main()
$python$,
    'JAVA', $java$import java.util.*;

public class Main {
  static class TreeNode {
    int value;
    TreeNode left;
    TreeNode right;

    TreeNode(int value) {
      this.value = value;
    }
  }

  public static class Codec {
    public String serialize(TreeNode root) {
      // TODO: return a string representation of the tree.
      return "";
    }

    public TreeNode deserialize(String data) {
      // TODO: rebuild and return the tree from serialize's output.
      return null;
    }
  }

  public static TreeNode buildTree(String[] tokens) {
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

  public static List<String> treeTokens(TreeNode root) {
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
$java$,
    'CPP', $cpp$#include <bits/stdc++.h>
using namespace std;

struct TreeNode {
    int value;
    TreeNode* left;
    TreeNode* right;
    TreeNode(int value) : value(value), left(nullptr), right(nullptr) {}
};

class Codec {
public:
    string serialize(TreeNode* root) {
        // TODO: return a string representation of the tree.
        return "";
    }

    TreeNode* deserialize(string data) {
        // TODO: rebuild and return the tree from serialize's output.
        return nullptr;
    }
};

TreeNode* buildTree(const vector<string>& tokens) {
    if (tokens.empty() || tokens[0] == "null") return nullptr;
    TreeNode* root = new TreeNode(stoi(tokens[0]));
    queue<TreeNode*> pending;
    pending.push(root);
    size_t index = 1;
    while (!pending.empty() && index < tokens.size()) {
        TreeNode* node = pending.front();
        pending.pop();
        if (index < tokens.size() && tokens[index] != "null") {
            node->left = new TreeNode(stoi(tokens[index]));
            pending.push(node->left);
        }
        index++;
        if (index < tokens.size() && tokens[index] != "null") {
            node->right = new TreeNode(stoi(tokens[index]));
            pending.push(node->right);
        }
        index++;
    }
    return root;
}

vector<string> treeTokens(TreeNode* root) {
    vector<string> tokens;
    if (root == nullptr) return tokens;
    queue<TreeNode*> pending;
    pending.push(root);
    while (!pending.empty()) {
        TreeNode* node = pending.front();
        pending.pop();
        if (node == nullptr) {
            tokens.push_back("null");
            continue;
        }
        tokens.push_back(to_string(node->value));
        pending.push(node->left);
        pending.push(node->right);
    }
    while (!tokens.empty() && tokens.back() == "null") tokens.pop_back();
    return tokens;
}

int main() {
    string line;
    getline(cin, line);
    istringstream input(line);
    vector<string> tokens;
    string token;
    while (input >> token) tokens.push_back(token);
    TreeNode* root = buildTree(tokens);
    Codec codec;
    TreeNode* restored = codec.deserialize(codec.serialize(root));
    vector<string> answer = treeTokens(restored);
    for (size_t i = 0; i < answer.size(); ++i) {
        if (i) cout << ' ';
        cout << answer[i];
    }
    cout << '\n';
    return 0;
}
$cpp$
  ),
  constraints_text = COALESCE(constraints_text, '') || E'\n\nInput format:\nLine 1: the tree in breadth-first order, space-separated. Use null for a missing child. A blank line represents an empty tree.\nOutput format:\nRun serialize and then deserialize, and print the restored tree in the same breadth-first format without trailing nulls.'
WHERE id = '00000000-0000-0000-0000-000000000015';
