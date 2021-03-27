package ui;

import java.util.*;

public final class Algorithms {
    private Algorithms() {}

    public static final Algorithm BREADTH_FIRST_SEARCH = ((stateSpace, heuristic) -> {
        Queue<Node> open = new LinkedList<>();
        open.add(new Node(stateSpace.getInitialState()));

        Set<String> closed = new HashSet<>();

        while (!open.isEmpty()) {
            Node n = open.remove();
            closed.add(n.state);

            if (stateSpace.isGoalState(n.state))
                return new Algorithm.SearchResult(n, closed.size());

            for (StateSpace.Successor m : stateSpace.getSuccessors(n.state))
                open.add(n.constructChild(m));
        }

        return null;
    });

    public static final Algorithm UNIFORM_COST_SEARCH = ((stateSpace, heuristic) -> {
        PriorityQueue<Node> open = new PriorityQueue<>((node1, node2) -> {
            if (node1.accumulatedCost == node2.accumulatedCost)
                return node1.state.compareTo(node2.state);

            return Double.compare(node1.accumulatedCost, node2.accumulatedCost);
        });
        open.add(new Node(stateSpace.getInitialState()));

        Set<String> closed = new HashSet<>();

        while (!open.isEmpty()) {
            Node n = open.remove();
            closed.add(n.state);

            if (stateSpace.isGoalState(n.state))
                return new Algorithm.SearchResult(n, closed.size());

            for (StateSpace.Successor m : stateSpace.getSuccessors(n.state))
                open.add(n.constructChild(m));
        }

        return null;
    });

    public static final Algorithm A_STAR_SEARCH = ((stateSpace, heuristic) -> {
        return null;
    });
}
