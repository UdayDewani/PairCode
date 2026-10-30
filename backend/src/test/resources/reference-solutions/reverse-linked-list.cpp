#include <bits/stdc++.h>
using namespace std;

vector<int> reverseList(const vector<int>& values) {
    return vector<int>(values.rbegin(), values.rend());
}

int main() {
    vector<int> values;
    int value;
    while (cin >> value) values.push_back(value);
    vector<int> answer = reverseList(values);
    for (size_t i = 0; i < answer.size(); ++i) {
        if (i) cout << ' ';
        cout << answer[i];
    }
    cout << '\n';
    return 0;
}
