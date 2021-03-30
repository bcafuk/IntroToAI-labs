package ui;

import java.util.*;

public final class SearchAlgorithms {
    private SearchAlgorithms() {}

    public static final SearchAlgorithm BREADTH_FIRST_SEARCH = ((stateSpace, heuristic) -> {
        Queue<Node> open = new LinkedList<>();
        Set<String> opened = new HashSet<>();

        Node initialNode = new Node(stateSpace.getInitialState());
        open.add(initialNode);
        opened.add(initialNode.state);

        while (!open.isEmpty()) {
            Node n = open.remove();

            if (stateSpace.isGoalState(n.state))
                return new SearchAlgorithm.SearchResult(n, opened.size() - open.size());

            for (StateSpace.Successor m : stateSpace.getSuccessors(n.state))
                if (opened.add(m.destination))
                    open.add(n.constructChild(m));
        }

        return null;
    });

    public static final SearchAlgorithm UNIFORM_COST_SEARCH = ((stateSpace, heuristic) -> {
        PriorityQueue<Node> open = new PriorityQueue<>((node1, node2) -> {
            if (node1.accumulatedCost == node2.accumulatedCost)
                return node1.state.compareTo(node2.state);

            return Double.compare(node1.accumulatedCost, node2.accumulatedCost);
        });
        open.add(new Node(stateSpace.getInitialState()));

        Set<String> closed = new HashSet<>();

        while (!open.isEmpty()) {
            Node n = open.remove();

            if (!closed.add(n.state))
                continue;

            if (stateSpace.isGoalState(n.state))
                return new SearchAlgorithm.SearchResult(n, closed.size());

            for (StateSpace.Successor m : stateSpace.getSuccessors(n.state)) {
                if (closed.contains(m.destination))
                    continue;

                open.add(n.constructChild(m));
            }
        }

        return null;
    });

    public static final SearchAlgorithm A_STAR_SEARCH = ((stateSpace, heuristic) -> {
        Comparator<Node> nodeComparator = (node1, node2) -> {
            double estCost1 = node1.accumulatedCost + heuristic.getEstimatedCost(node1.state);
            double estCost2 = node2.accumulatedCost + heuristic.getEstimatedCost(node2.state);

            if (estCost1 == estCost2)
                return node1.state.compareTo(node2.state);

            return Double.compare(estCost1, estCost2);
        };

        Map<String, Node> openNodes = new HashMap<>();
        TreeMap<Node, Node> open = new TreeMap<>(nodeComparator);
        Node initialNode = new Node(stateSpace.getInitialState());

        open.put(initialNode, initialNode);
        openNodes.put(initialNode.state, initialNode);

        Map<String, Double> closed = new HashMap<>();

        while (!open.isEmpty()) {
            Node n = open.firstKey();
            open.remove(n);
            openNodes.remove(n.state);

            closed.put(n.state, n.accumulatedCost);

            if (stateSpace.isGoalState(n.state))
                return new SearchAlgorithm.SearchResult(n, closed.size());

            for (StateSpace.Successor s : stateSpace.getSuccessors(n.state)) {
                Node m = n.constructChild(s);

                if (openNodes.containsKey(m.state)) {
                    Node mPrime = openNodes.get(m.state);
                    if (mPrime.accumulatedCost < m.accumulatedCost) {
                        continue;
                    } else {
                        open.remove(mPrime);
                        openNodes.remove(mPrime.state);
                    }
                }

                if (closed.containsKey(m.state)) {
                    if (closed.get(m.state) < m.accumulatedCost)
                        continue;
                    else
                        closed.remove(m.state);
                }

                open.put(m, m);
                openNodes.put(m.state, m);
            }
        }

        return null;
    });
}
