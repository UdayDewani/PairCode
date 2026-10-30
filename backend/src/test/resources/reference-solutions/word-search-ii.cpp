#include <bits/stdc++.h>
using namespace std;

bool search(vector<vector<char>>& board, const string& word, int row, int col, int index) {
    if (index == static_cast<int>(word.size())) return true;
    if (row < 0 || row >= static_cast<int>(board.size()) || col < 0
        || col >= static_cast<int>(board[row].size())) {
        return false;
    }
    if (board[row][col] != word[index]) return false;
    char saved = board[row][col];
    board[row][col] = '#';
    bool found = search(board, word, row + 1, col, index + 1)
        || search(board, word, row - 1, col, index + 1)
        || search(board, word, row, col + 1, index + 1)
        || search(board, word, row, col - 1, index + 1);
    board[row][col] = saved;
    return found;
}

bool exists(vector<vector<char>>& board, const string& word) {
    for (int row = 0; row < static_cast<int>(board.size()); ++row) {
        for (int col = 0; col < static_cast<int>(board[row].size()); ++col) {
            if (board[row][col] == word[0] && search(board, word, row, col, 0)) return true;
        }
    }
    return false;
}

vector<string> findWords(vector<vector<char>>& board, vector<string>& words) {
    vector<string> found;
    for (const string& word : words) {
        if (exists(board, word)) found.push_back(word);
    }
    return found;
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
