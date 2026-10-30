import sys
from collections import defaultdict


def group_anagrams(words):
    groups = defaultdict(list)
    for word in words:
        groups["".join(sorted(word))].append(word)
    return list(groups.values())


def main():
    words = sys.stdin.read().split()
    groups = group_anagrams(words)
    lines = [" ".join(sorted(group)) for group in groups if group]
    for line in sorted(lines):
        print(line)


if __name__ == "__main__":
    main()
