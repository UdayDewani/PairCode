#include <bits/stdc++.h>
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
        if (root == nullptr) return "";
        vector<string> tokens;
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
        string output;
        for (size_t i = 0; i < tokens.size(); ++i) {
            if (i) output += ' ';
            output += tokens[i];
        }
        return output;
    }

    TreeNode* deserialize(string data) {
        istringstream input(data);
        vector<string> tokens;
        string token;
        while (input >> token) tokens.push_back(token);
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
