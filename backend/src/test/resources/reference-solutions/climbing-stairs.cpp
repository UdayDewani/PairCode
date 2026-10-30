#include <bits/stdc++.h>
using namespace std;

int climbStairs(int n) {
    int previous = 1;
    int current = 1;
    for (int i = 0; i < n; ++i) {
        int next = previous + current;
        previous = current;
        current = next;
    }
    return previous;
}

int main() {
    int n;
    cin >> n;
    cout << climbStairs(n) << '\n';
    return 0;
}
