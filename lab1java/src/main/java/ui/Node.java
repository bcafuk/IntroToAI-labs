package ui;

import java.util.Objects;

public class Node {
    public final String state;
    public final int depth;
    public final Node parent;
    public final double accumulatedCost;

    public Node(String state) {
        this(state, 0, null, 0);
    }

    private Node(String state, int depth, Node parent, double accumulatedCost) {
        this.state = Objects.requireNonNull(state);
        this.depth = depth;
        this.parent = parent;
        this.accumulatedCost = accumulatedCost;
    }

    public Node constructChild(StateSpace.Successor successor) {
        return new Node(successor.destination, depth + 1, this, accumulatedCost + successor.cost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return state.equals(node.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}
