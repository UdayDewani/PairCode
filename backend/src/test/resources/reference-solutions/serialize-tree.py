import sys


class TreeNode:
    def __init__(self, value=0, left=None, right=None):
        self.value = value
        self.left = left
        self.right = right


class Codec:
    def serialize(self, root):
        if root is None:
            return ""
        tokens = []
        queue = [root]
        while queue:
            node = queue.pop(0)
            if node is None:
                tokens.append("null")
                continue
            tokens.append(str(node.value))
            queue.append(node.left)
            queue.append(node.right)
        while tokens and tokens[-1] == "null":
            tokens.pop()
        return " ".join(tokens)

    def deserialize(self, data):
        tokens = data.split()
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


def tree_tokens(root):
    if root is None:
        return []
    tokens = []
    queue = [root]
    while queue:
        node = queue.pop(0)
        if node is None:
            tokens.append("null")
            continue
        tokens.append(str(node.value))
        queue.append(node.left)
        queue.append(node.right)
    while tokens and tokens[-1] == "null":
        tokens.pop()
    return tokens


def main():
    tokens = sys.stdin.read().split()
    root = build_tree(tokens)
    codec = Codec()
    restored = codec.deserialize(codec.serialize(root))
    print(" ".join(tree_tokens(restored)))


if __name__ == "__main__":
    main()
