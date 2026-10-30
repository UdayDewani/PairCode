import sys


def find_words(board, words):
    if not board or not board[0]:
        return []
    rows = len(board)
    cols = len(board[0])

    def exists(word):
        def search(row, col, index):
            if index == len(word):
                return True
            if row < 0 or row >= rows or col < 0 or col >= cols:
                return False
            if board[row][col] != word[index]:
                return False
            saved = board[row][col]
            board[row][col] = "#"
            found = (
                search(row + 1, col, index + 1)
                or search(row - 1, col, index + 1)
                or search(row, col + 1, index + 1)
                or search(row, col - 1, index + 1)
            )
            board[row][col] = saved
            return found

        for row in range(rows):
            for col in range(cols):
                if board[row][col] == word[0] and search(row, col, 0):
                    return True
        return False

    found = []
    for word in words:
        if exists(word):
            found.append(word)
    return found


def main():
    lines = [line.strip() for line in sys.stdin.read().splitlines() if line.strip()]
    board = []
    words = []
    reading_board = True
    for line in lines:
        tokens = line.split()
        if reading_board and all(len(token) == 1 for token in tokens):
            board.append(tokens)
        else:
            reading_board = False
            words.append(line)
    for word in sorted(find_words(board, words)):
        print(word)


if __name__ == "__main__":
    main()
