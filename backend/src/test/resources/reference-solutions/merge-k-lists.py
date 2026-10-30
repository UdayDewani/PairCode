import sys


def merge_k_lists(lists):
    values = [value for values in lists for value in values]
    values.sort()
    return values


def main():
    lines = sys.stdin.read().splitlines()
    lists = [[int(value) for value in line.split()] for line in lines]
    merged = merge_k_lists(lists)
    print(" ".join(str(value) for value in merged))


if __name__ == "__main__":
    main()
