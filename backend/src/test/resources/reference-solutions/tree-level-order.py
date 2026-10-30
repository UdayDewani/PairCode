import sys


class TreeNode:
    def __init__(self, value=0, left=None, right=None):
        self.value = value
        self.left = left
        self.right = right


def build_tree(tokens):
    if not tokens or tokens[0] == "null":
        return None
    root = TreeNode(int(tokens[0]))
    queue = [root]
    index = 1
    while queue and index < len(tokens):
        node = queue.pop(0)
        if index < len(tokens) and tokens[index] != "null":
            node.left = TreeNode(int(tokens[index]))
            queue.append(node.left)
        index += 1
        if index < len(tokens) and tokens[index] != "null":
            node.right = TreeNode(int(tokens[index]))
            queue.append(node.right)
        index += 1
    return root


def level_order(root):
    if root is None:
        return []
    levels = []
    queue = [root]
    while queue:
        level = []
        next_queue = []
        for node in queue:
            level.append(node.value)
            if node.left:
                next_queue.append(node.left)
            if node.right:
                next_queue.append(node.right)
        levels.append(level)
        queue = next_queue
    return levels


def main():
    tokens = sys.stdin.read().split()
    root = build_tree(tokens)
    for level in level_order(root):
        print(" ".join(str(value) for value in level))


if __name__ == "__main__":
    main()
