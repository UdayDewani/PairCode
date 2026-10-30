#include <bits/stdc++.h>
using namespace std;

vector<vector<int>> merge(vector<vector<int>>& intervals) {
    vector<vector<int>> answer;
    if (intervals.empty()) return answer;
    sort(intervals.begin(), intervals.end());
    answer.push_back(intervals[0]);
    for (size_t i = 1; i < intervals.size(); ++i) {
        if (intervals[i][0] <= answer.back()[1]) {
            answer.back()[1] = max(answer.back()[1], intervals[i][1]);
        } else {
            answer.push_back(intervals[i]);
        }
    }
    return answer;
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
