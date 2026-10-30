#include <bits/stdc++.h>
using namespace std;

bool isValid(const string& s) {
    stack<char> pending;
    for (char c : s) {
        if (c == '(' || c == '[' || c == '{') {
            pending.push(c);
        } else {
            if (pending.empty()) return false;
            char open = pending.top();
            pending.pop();
            if ((c == ')' && open != '(') || (c == ']' && open != '[') || (c == '}' && open != '{')) {
                return false;
            }
        }
    }
    return pending.empty();
}

int main() {
    string s;
    getline(cin, s);
    cout << (isValid(s) ? "true" : "false") << '\n';
    return 0;
}
