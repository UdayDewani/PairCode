import sys


def num_islands(grid):
    if not grid:
        return 0
    rows = len(grid)
    cols = len(grid[0])
    visited = set()
    count = 0
    for row in range(rows):
        for col in range(cols):
            if grid[row][col] != "1" or (row, col) in visited:
                continue
            count += 1
            stack = [(row, col)]
            visited.add((row, col))
            while stack:
                current_row, current_col = stack.pop()
                for row_delta, col_delta in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    next_row = current_row + row_delta
                    next_col = current_col + col_delta
                    if (
                        0 <= next_row < rows
                        and 0 <= next_col < cols
                        and grid[next_row][next_col] == "1"
                        and (next_row, next_col) not in visited
                    ):
                        visited.add((next_row, next_col))
                        stack.append((next_row, next_col))
    return count


def main():
    grid = [line.split() for line in sys.stdin.read().splitlines() if line.strip()]
    print(num_islands(grid))


if __name__ == "__main__":
    main()
