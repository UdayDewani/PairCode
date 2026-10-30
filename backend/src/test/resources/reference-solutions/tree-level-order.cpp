#include <bits/stdc++.h>
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
        ++index;
        if (index < tokens.size() && tokens[index] != "null") {
            node->right = new TreeNode(stoi(tokens[index]));
            pending.push(node->right);
        }
        ++index;
    }
    return root;
}

vector<vector<int>> levelOrder(TreeNode* root) {
    vector<vector<int>> levels;
    if (root == nullptr) return levels;
    queue<TreeNode*> pending;
    pending.push(root);
    while (!pending.empty()) {
        int size = pending.size();
        vector<int> level;
        for (int i = 0; i < size; ++i) {
            TreeNode* node = pending.front();
            pending.pop();
            level.push_back(node->value);
            if (node->left) pending.push(node->left);
            if (node->right) pending.push(node->right);
        }
        levels.push_back(level);
    }
    return levels;
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
