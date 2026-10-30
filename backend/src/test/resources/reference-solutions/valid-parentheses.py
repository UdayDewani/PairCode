import sys


def is_valid(s):
    stack = []
    pairs = {")": "(", "]": "[", "}": "{"}
    for char in s:
        if char in "([{":
            stack.append(char)
        elif not stack or stack.pop() != pairs[char]:
            return False
    return not stack


def main():
    s = sys.stdin.read().strip()
    print("true" if is_valid(s) else "false")


if __name__ == "__main__":
    main()
