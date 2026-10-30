import sys


def max_profit(prices):
    lowest = None
    best = 0
    for price in prices:
        if lowest is None or price < lowest:
            lowest = price
        best = max(best, price - lowest)
    return best


def main():
    prices = [int(value) for value in sys.stdin.read().split()]
    print(max_profit(prices))


if __name__ == "__main__":
    main()
