#include <bits/stdc++.h>
using namespace std;

vector<vector<string>> groupAnagrams(const vector<string>& words) {
    map<string, vector<string>> groups;
    for (const string& word : words) {
        string key = word;
        sort(key.begin(), key.end());
        groups[key].push_back(word);
    }
    vector<vector<string>> answer;
    for (auto& entry : groups) answer.push_back(entry.second);
    return answer;
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
