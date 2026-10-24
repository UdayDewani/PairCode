-- V6: Real test cases for all 15 seeded problems.
-- Replace V4's empty placeholder rows with actual input/expected pairs.
-- We do NOT modify V3/V4 (already applied); we delete their empty rows and insert real ones.

-- Remove test results referencing placeholder test cases to satisfy FK constraint
DELETE FROM test_results
WHERE test_case_id IN (
  SELECT md5(id::text || ':visible')::uuid FROM problems WHERE id <> '00000000-0000-0000-0000-000000000001'
  UNION ALL
  SELECT md5(id::text || ':hidden')::uuid FROM problems WHERE id <> '00000000-0000-0000-0000-000000000001'
);

-- Remove empty placeholder rows from V4 for all problems except the one already covered in V3 (problem 1)
DELETE FROM test_cases
WHERE id IN (
  SELECT md5(id::text || ':visible')::uuid FROM problems WHERE id <> '00000000-0000-0000-0000-000000000001'
  UNION ALL
  SELECT md5(id::text || ':hidden')::uuid FROM problems WHERE id <> '00000000-0000-0000-0000-000000000001'
);

-- Problem 2: Valid Parentheses
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p2:v1')::uuid,'00000000-0000-0000-0000-000000000002',E'()\n',E'true\n',false,1),
(md5('p2:h1')::uuid,'00000000-0000-0000-0000-000000000002',E'()[]{}\n',E'true\n',true,2),
(md5('p2:h2')::uuid,'00000000-0000-0000-0000-000000000002',E'(]\n',E'false\n',true,3),
(md5('p2:h3')::uuid,'00000000-0000-0000-0000-000000000002',E'([)]\n',E'false\n',true,4);

-- Problem 3: Best Time to Buy and Sell Stock
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p3:v1')::uuid,'00000000-0000-0000-0000-000000000003',E'7 1 5 3 6 4\n',E'5\n',false,1),
(md5('p3:h1')::uuid,'00000000-0000-0000-0000-000000000003',E'7 6 4 3 1\n',E'0\n',true,2);

-- Problem 4: Reverse Linked List (represented as space-sep list)
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p4:v1')::uuid,'00000000-0000-0000-0000-000000000004',E'1 2 3 4 5\n',E'5 4 3 2 1\n',false,1),
(md5('p4:h1')::uuid,'00000000-0000-0000-0000-000000000004',E'1 2\n',E'2 1\n',true,2),
(md5('p4:h2')::uuid,'00000000-0000-0000-0000-000000000004',E'1\n',E'1\n',true,3);

-- Problem 5: Climbing Stairs
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p5:v1')::uuid,'00000000-0000-0000-0000-000000000005',E'2\n',E'2\n',false,1),
(md5('p5:h1')::uuid,'00000000-0000-0000-0000-000000000005',E'3\n',E'3\n',true,2),
(md5('p5:h2')::uuid,'00000000-0000-0000-0000-000000000005',E'10\n',E'89\n',true,3);

-- Problem 6: Longest Substring Without Repeating Characters
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p6:v1')::uuid,'00000000-0000-0000-0000-000000000006',E'abcabcbb\n',E'3\n',false,1),
(md5('p6:h1')::uuid,'00000000-0000-0000-0000-000000000006',E'bbbbb\n',E'1\n',true,2),
(md5('p6:h2')::uuid,'00000000-0000-0000-0000-000000000006',E'pwwkew\n',E'3\n',true,3);

-- Problem 7: Group Anagrams (output sorted lines, each group sorted and joined by comma)
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p7:v1')::uuid,'00000000-0000-0000-0000-000000000007',E'eat tea tan ate nat bat\n',E'ate eat tea\nbat\nnat tan\n',false,1),
(md5('p7:h1')::uuid,'00000000-0000-0000-0000-000000000007',E'a\n',E'a\n',true,2);

-- Problem 8: Number of Islands
-- Grid rows separated by newline, cells by space
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p8:v1')::uuid,'00000000-0000-0000-0000-000000000008',E'1 1 1 1 0\n1 1 0 1 0\n1 1 0 0 0\n0 0 0 0 0\n',E'1\n',false,1),
(md5('p8:h1')::uuid,'00000000-0000-0000-0000-000000000008',E'1 1 0 0 0\n1 1 0 0 0\n0 0 1 0 0\n0 0 0 1 1\n',E'3\n',true,2);

-- Problem 9: Binary Tree Level Order (input: space-sep BFS with null)
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p9:v1')::uuid,'00000000-0000-0000-0000-000000000009',E'3 9 20 null null 15 7\n',E'3\n9 20\n15 7\n',false,1),
(md5('p9:h1')::uuid,'00000000-0000-0000-0000-000000000009',E'1\n',E'1\n',true,2);

-- Problem 10: Merge Intervals (each line: start end)
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p10:v1')::uuid,'00000000-0000-0000-0000-000000000010',E'1 3\n2 6\n8 10\n15 18\n',E'1 6\n8 10\n15 18\n',false,1),
(md5('p10:h1')::uuid,'00000000-0000-0000-0000-000000000010',E'1 4\n4 5\n',E'1 5\n',true,2);

-- Problem 11: Merge K Sorted Lists (k lists on k lines, space-sep; output one line)
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p11:v1')::uuid,'00000000-0000-0000-0000-000000000011',E'1 4 5\n1 3 4\n2 6\n',E'1 1 2 3 4 4 5 6\n',false,1),
(md5('p11:h1')::uuid,'00000000-0000-0000-0000-000000000011',E'\n',E'\n',true,2);

-- Problem 12: Trapping Rain Water
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p12:v1')::uuid,'00000000-0000-0000-0000-000000000012',E'0 1 0 2 1 0 1 3 2 1 2 1\n',E'6\n',false,1),
(md5('p12:h1')::uuid,'00000000-0000-0000-0000-000000000012',E'4 2 0 3 2 5\n',E'9\n',true,2);

-- Problem 13: Minimum Window Substring (line1: s, line2: t)
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p13:v1')::uuid,'00000000-0000-0000-0000-000000000013',E'ADOBECODEBANC\nABC\n',E'BANC\n',false,1),
(md5('p13:h1')::uuid,'00000000-0000-0000-0000-000000000013',E'a\na\n',E'a\n',true,2);

-- Problem 14: Word Search II (rows of board, then words line-by-line; output found words sorted)
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p14:v1')::uuid,'00000000-0000-0000-0000-000000000014',E'o a a n\ne t a e\ni h k r\ni f l v\noath\npea\neat\nrain\n',E'eat\noath\n',false,1),
(md5('p14:h1')::uuid,'00000000-0000-0000-0000-000000000014',E'a b\nc d\nab\n',E'ab\n',true,2);

-- Problem 15: Serialize / Deserialize Binary Tree
-- Round-trip: serialize then deserialize, output same BFS string
INSERT INTO test_cases (id,problem_id,input,expected_output,is_hidden,ordinal) VALUES
(md5('p15:v1')::uuid,'00000000-0000-0000-0000-000000000015',E'1 2 3 null null 4 5\n',E'1 2 3 null null 4 5\n',false,1),
(md5('p15:h1')::uuid,'00000000-0000-0000-0000-000000000015',E'\n',E'\n',true,2);

-- Update problems 2-15 with proper examples and starter code
UPDATE problems SET
  examples='[{"input":"s = \"()\"","output":"true"},{"input":"s = \"()[]{\"","output":"false"}]',
  starter_code='{"PYTHON":"def is_valid(s: str) -> bool:\n    pass","JAVA":"class Solution { public boolean isValid(String s) { return false; } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nbool isValid(string s) { return false; }"}'
WHERE id='00000000-0000-0000-0000-000000000002';

UPDATE problems SET
  examples='[{"input":"prices = [7,1,5,3,6,4]","output":"5"},{"input":"prices = [7,6,4,3,1]","output":"0"}]',
  starter_code='{"PYTHON":"def max_profit(prices):\n    pass","JAVA":"class Solution { public int maxProfit(int[] prices) { return 0; } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nint maxProfit(vector<int>& prices) { return 0; }"}'
WHERE id='00000000-0000-0000-0000-000000000003';

UPDATE problems SET
  examples='[{"input":"head = [1,2,3,4,5]","output":"[5,4,3,2,1]"}]',
  starter_code='{"PYTHON":"def reverse_list(head):\n    pass","JAVA":"class Solution { public ListNode reverseList(ListNode head) { return null; } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nListNode* reverseList(ListNode* head) { return nullptr; }"}'
WHERE id='00000000-0000-0000-0000-000000000004';

UPDATE problems SET
  examples='[{"input":"n = 2","output":"2"},{"input":"n = 3","output":"3"}]',
  starter_code='{"PYTHON":"def climb_stairs(n: int) -> int:\n    pass","JAVA":"class Solution { public int climbStairs(int n) { return 0; } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nint climbStairs(int n) { return 0; }"}'
WHERE id='00000000-0000-0000-0000-000000000005';

UPDATE problems SET
  examples='[{"input":"s = \"abcabcbb\"","output":"3"},{"input":"s = \"bbbbb\"","output":"1"}]',
  starter_code='{"PYTHON":"def length_of_longest_substring(s: str) -> int:\n    pass","JAVA":"class Solution { public int lengthOfLongestSubstring(String s) { return 0; } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nint lengthOfLongestSubstring(string s) { return 0; }"}'
WHERE id='00000000-0000-0000-0000-000000000006';

UPDATE problems SET
  examples='[{"input":"strs = [\"eat\",\"tea\",\"tan\",\"ate\",\"nat\",\"bat\"]","output":"[[\"bat\"],[\"nat\",\"tan\"],[\"ate\",\"eat\",\"tea\"]]"}]',
  starter_code='{"PYTHON":"def group_anagrams(strs):\n    pass","JAVA":"class Solution { public List<List<String>> groupAnagrams(String[] strs) { return new ArrayList<>(); } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nvector<vector<string>> groupAnagrams(vector<string>& strs) { return {}; }"}'
WHERE id='00000000-0000-0000-0000-000000000007';

UPDATE problems SET
  examples='[{"input":"grid = [[\"1\",\"1\",\"1\",\"1\",\"0\"],[\"1\",\"1\",\"0\",\"1\",\"0\"]]","output":"1"}]',
  starter_code='{"PYTHON":"def num_islands(grid):\n    pass","JAVA":"class Solution { public int numIslands(char[][] grid) { return 0; } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nint numIslands(vector<vector<char>>& grid) { return 0; }"}'
WHERE id='00000000-0000-0000-0000-000000000008';

UPDATE problems SET
  examples='[{"input":"root = [3,9,20,null,null,15,7]","output":"[[3],[9,20],[15,7]]"}]',
  starter_code='{"PYTHON":"def level_order(root):\n    pass","JAVA":"class Solution { public List<List<Integer>> levelOrder(TreeNode root) { return new ArrayList<>(); } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nvector<vector<int>> levelOrder(TreeNode* root) { return {}; }"}'
WHERE id='00000000-0000-0000-0000-000000000009';

UPDATE problems SET
  examples='[{"input":"intervals = [[1,3],[2,6],[8,10],[15,18]]","output":"[[1,6],[8,10],[15,18]]"}]',
  starter_code='{"PYTHON":"def merge(intervals):\n    pass","JAVA":"class Solution { public int[][] merge(int[][] intervals) { return new int[][]{}; } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nvector<vector<int>> merge(vector<vector<int>>& intervals) { return {}; }"}'
WHERE id='00000000-0000-0000-0000-000000000010';

UPDATE problems SET
  examples='[{"input":"lists = [[1,4,5],[1,3,4],[2,6]]","output":"[1,1,2,3,4,4,5,6]"}]',
  starter_code='{"PYTHON":"def merge_k_lists(lists):\n    pass","JAVA":"class Solution { public ListNode mergeKLists(ListNode[] lists) { return null; } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nListNode* mergeKLists(vector<ListNode*>& lists) { return nullptr; }"}'
WHERE id='00000000-0000-0000-0000-000000000011';

UPDATE problems SET
  examples='[{"input":"height = [0,1,0,2,1,0,1,3,2,1,2,1]","output":"6"}]',
  starter_code='{"PYTHON":"def trap(height):\n    pass","JAVA":"class Solution { public int trap(int[] height) { return 0; } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nint trap(vector<int>& height) { return 0; }"}'
WHERE id='00000000-0000-0000-0000-000000000012';

UPDATE problems SET
  examples='[{"input":"s = \"ADOBECODEBANC\", t = \"ABC\"","output":"\"BANC\""}]',
  starter_code='{"PYTHON":"def min_window(s: str, t: str) -> str:\n    pass","JAVA":"class Solution { public String minWindow(String s, String t) { return \"\"; } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nstring minWindow(string s, string t) { return \"\"; }"}'
WHERE id='00000000-0000-0000-0000-000000000013';

UPDATE problems SET
  examples='[{"input":"board = [[\"o\",\"a\",\"a\",\"n\"],[\"e\",\"t\",\"a\",\"e\"],[\"i\",\"h\",\"k\",\"r\"],[\"i\",\"f\",\"l\",\"v\"]], words = [\"oath\",\"pea\",\"eat\",\"rain\"]","output":"[\"eat\",\"oath\"]"}]',
  starter_code='{"PYTHON":"def find_words(board, words):\n    pass","JAVA":"class Solution { public List<String> findWords(char[][] board, String[] words) { return new ArrayList<>(); } }","CPP":"#include <bits/stdc++.h>\nusing namespace std;\nvector<string> findWords(vector<vector<char>>& board, vector<string>& words) { return {}; }"}'
WHERE id='00000000-0000-0000-0000-000000000014';

UPDATE problems SET
  examples='[{"input":"root = [1,2,3,null,null,4,5]","output":"\"1,2,3,null,null,4,5\""}]',
  starter_code='{"PYTHON":"class Codec:\n    def serialize(self, root): pass\n    def deserialize(self, data): pass","JAVA":"public class Codec { public String serialize(TreeNode root) { return \"\"; } public TreeNode deserialize(String data) { return null; } }","CPP":"class Codec { public: string serialize(TreeNode* root) { return \"\"; } TreeNode* deserialize(string data) { return nullptr; } };"}'
WHERE id='00000000-0000-0000-0000-000000000015';
