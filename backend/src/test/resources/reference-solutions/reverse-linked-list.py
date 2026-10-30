import sys


def reverse_list(values):
    return list(reversed(values))


def main():
    values = [int(value) for value in sys.stdin.read().split()]
    answer = reverse_list(values)
    print(" ".join(str(value) for value in answer))


if __name__ == "__main__":
    main()
