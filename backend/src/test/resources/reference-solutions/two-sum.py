import sys


def two_sum(nums, target):
    seen = {}
    for index, value in enumerate(nums):
        needed = target - value
        if needed in seen:
            return [seen[needed], index]
        seen[value] = index
    return []


def main():
    lines = sys.stdin.read().splitlines()
    nums = [int(value) for value in lines[0].split()]
    target = int(lines[1])
    answer = two_sum(nums, target)
    print(" ".join(str(index) for index in answer))


if __name__ == "__main__":
    main()
