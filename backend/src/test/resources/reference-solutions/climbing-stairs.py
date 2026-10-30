import sys


def climb_stairs(n):
    previous = 1
    current = 1
    for _ in range(n):
        previous, current = current, previous + current
    return previous


def main():
    n = int(sys.stdin.read().strip())
    print(climb_stairs(n))


if __name__ == "__main__":
    main()
