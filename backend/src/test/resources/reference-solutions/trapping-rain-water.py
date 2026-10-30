import sys


def trap(height):
    if not height:
        return 0
    left = 0
    right = len(height) - 1
    left_max = 0
    right_max = 0
    water = 0
    while left < right:
        if height[left] < height[right]:
            left_max = max(left_max, height[left])
            water += left_max - height[left]
            left += 1
        else:
            right_max = max(right_max, height[right])
            water += right_max - height[right]
            right -= 1
    return water


def main():
    height = [int(value) for value in sys.stdin.read().split()]
    print(trap(height))


if __name__ == "__main__":
    main()
