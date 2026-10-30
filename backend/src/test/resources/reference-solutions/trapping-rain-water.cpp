#include <bits/stdc++.h>
using namespace std;

int trap(const vector<int>& height) {
    if (height.empty()) return 0;
    int left = 0;
    int right = static_cast<int>(height.size()) - 1;
    int leftMax = 0;
    int rightMax = 0;
    int water = 0;
    while (left < right) {
        if (height[left] < height[right]) {
            leftMax = max(leftMax, height[left]);
            water += leftMax - height[left];
            ++left;
        } else {
            rightMax = max(rightMax, height[right]);
            water += rightMax - height[right];
            --right;
        }
    }
    return water;
}

int main() {
    vector<int> height;
    int value;
    while (cin >> value) height.push_back(value);
    cout << trap(height) << '\n';
    return 0;
}
