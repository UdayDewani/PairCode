import sys
from collections import Counter


def min_window(s, t):
    if not t:
        return ""
    need = Counter(t)
    missing = len(t)
    left = 0
    best = None
    for right, char in enumerate(s):
        if need[char] > 0:
            missing -= 1
        need[char] -= 1
        while missing == 0:
            if best is None or right - left < best[1] - best[0]:
                best = (left, right)
            need[s[left]] += 1
            if need[s[left]] > 0:
                missing += 1
            left += 1
    return "" if best is None else s[best[0]:best[1] + 1]


def main():
    lines = sys.stdin.read().splitlines()
    s = lines[0] if lines else ""
    t = lines[1] if len(lines) > 1 else ""
    print(min_window(s, t))


if __name__ == "__main__":
    main()
