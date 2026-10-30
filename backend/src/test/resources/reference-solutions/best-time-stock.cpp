#include <bits/stdc++.h>
using namespace std;

int maxProfit(const vector<int>& prices) {
    int lowest = INT_MAX;
    int best = 0;
    for (int price : prices) {
        lowest = min(lowest, price);
        best = max(best, price - lowest);
    }
    return best;
}

int main() {
    vector<int> prices;
    int value;
    while (cin >> value) prices.push_back(value);
    cout << maxProfit(prices) << '\n';
    return 0;
}
