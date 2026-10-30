#include <bits/stdc++.h>
using namespace std;

string minWindow(const string& s, const string& t) {
    if (t.empty()) return "";
    unordered_map<char, int> need;
    for (char c : t) ++need[c];
    int missing = t.size();
    int left = 0;
    int bestStart = 0;
    int bestLength = INT_MAX;
    for (int right = 0; right < static_cast<int>(s.size()); ++right) {
        char c = s[right];
        if (need[c] > 0) --missing;
        --need[c];
        while (missing == 0) {
            if (right - left + 1 < bestLength) {
                bestStart = left;
                bestLength = right - left + 1;
            }
            char removed = s[left];
            ++need[removed];
            if (need[removed] > 0) ++missing;
            ++left;
        }
    }
    return bestLength == INT_MAX ? "" : s.substr(bestStart, bestLength);
}

int main() {
    string s;
    string t;
    getline(cin, s);
    getline(cin, t);
    cout << minWindow(s, t) << '\n';
    return 0;
}
