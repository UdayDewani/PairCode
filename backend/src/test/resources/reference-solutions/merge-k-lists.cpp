#include <bits/stdc++.h>
using namespace std;

vector<int> mergeKLists(const vector<vector<int>>& lists) {
    vector<int> merged;
    for (const vector<int>& values : lists) merged.insert(merged.end(), values.begin(), values.end());
    sort(merged.begin(), merged.end());
    return merged;
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
