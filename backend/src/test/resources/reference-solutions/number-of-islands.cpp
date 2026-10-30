#include <bits/stdc++.h>
using namespace std;

int numIslands(vector<vector<char>>& grid) {
    if (grid.empty() || grid[0].empty()) return 0;
    int rows = grid.size();
    int cols = grid[0].size();
    vector<vector<bool>> visited(rows, vector<bool>(cols, false));
    int count = 0;
    const int dr[] = {1, -1, 0, 0};
    const int dc[] = {0, 0, 1, -1};
    for (int row = 0; row < rows; ++row) {
        for (int col = 0; col < cols; ++col) {
            if (grid[row][col] != '1' || visited[row][col]) continue;
            ++count;
            stack<pair<int, int>> pending;
            pending.push({row, col});
            visited[row][col] = true;
            while (!pending.empty()) {
                auto [currentRow, currentCol] = pending.top();
                pending.pop();
                for (int direction = 0; direction < 4; ++direction) {
                    int nextRow = currentRow + dr[direction];
                    int nextCol = currentCol + dc[direction];
                    if (nextRow >= 0 && nextRow < rows && nextCol >= 0 && nextCol < cols
                        && grid[nextRow][nextCol] == '1' && !visited[nextRow][nextCol]) {
                        visited[nextRow][nextCol] = true;
                        pending.push({nextRow, nextCol});
                    }
                }
            }
        }
    }
    return count;
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
