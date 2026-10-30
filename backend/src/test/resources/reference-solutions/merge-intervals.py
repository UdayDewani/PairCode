import sys


def merge(intervals):
    if not intervals:
        return []
    ordered = sorted(intervals, key=lambda interval: interval[0])
    merged = [list(ordered[0])]
    for start, end in ordered[1:]:
        if start <= merged[-1][1]:
            merged[-1][1] = max(merged[-1][1], end)
        else:
            merged.append([start, end])
    return merged


def main():
    intervals = []
    for line in sys.stdin.read().splitlines():
        if line.strip():
            start, end = (int(value) for value in line.split())
            intervals.append([start, end])
    for start, end in merge(intervals):
        print(start, end)


if __name__ == "__main__":
    main()
