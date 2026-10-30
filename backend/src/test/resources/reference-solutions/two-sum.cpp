#include <bits/stdc++.h>
using namespace std;

vector<int> twoSum(vector<int>& nums, int target) {
    unordered_map<int, int> seen;
    for (int i = 0; i < static_cast<int>(nums.size()); ++i) {
        int needed = target - nums[i];
        auto found = seen.find(needed);
        if (found != seen.end()) return {found->second, i};
        seen[nums[i]] = i;
    }
    return {};
}

int main() {
    string line;
    getline(cin, line);
    istringstream first(line);
    vector<int> nums;
    int value;
    while (first >> value) nums.push_back(value);
    int target;
    cin >> target;
    vector<int> answer = twoSum(nums, target);
    for (size_t i = 0; i < answer.size(); ++i) {
        if (i) cout << ' ';
        cout << answer[i];
    }
    cout << '\n';
    return 0;
}
