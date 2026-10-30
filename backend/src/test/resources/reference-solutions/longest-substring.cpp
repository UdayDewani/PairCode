#include <bits/stdc++.h>
using namespace std;

int lengthOfLongestSubstring(const string& s) {
    unordered_map<char, int> lastSeen;
    int left = 0;
    int best = 0;
    for (int right = 0; right < static_cast<int>(s.size()); ++right) {
        char c = s[right];
        auto found = lastSeen.find(c);
        if (found != lastSeen.end() && found->second >= left) {
            left = found->second + 1;
        }
        lastSeen[c] = right;
        best = max(best, right - left + 1);
    }
    return best;
}

int main() {
    string s;
    getline(cin, s);
    cout << lengthOfLongestSubstring(s) << '\n';
    return 0;
}
