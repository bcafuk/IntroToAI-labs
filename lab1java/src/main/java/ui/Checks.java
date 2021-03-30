package ui;

import java.util.*;

public final class Checks {
    private Checks() {}

    public static void isOptimistic(StateSpace stateSpace, Heuristic heuristic) {
        PriorityQueue<Node> open = new PriorityQueue<>((node1, node2) -> {
            if (node1.accumulatedCost == node2.accumulatedCost)
                return node1.state.compareTo(node2.state);

            return Double.compare(node1.accumulatedCost, node2.accumulatedCost);
        });

        Map<String, Double> costs = new TreeMap<>();

        stateSpace.getGoalStates().stream().map(Node::new).forEach(n -> {
            open.add(n);
            costs.put(n.state, n.accumulatedCost);
        });

        Set<String> closed = new HashSet<>();

        while (!open.isEmpty()) {
            Node n = open.remove();

            if (closed.contains(n.state))
                continue;

            closed.add(n.state);

            for (StateSpace.Successor s : stateSpace.getPredecessors(n.state)) {
                Node m = n.constructChild(s);

                if (closed.contains(m.state) || costs.containsKey(m.state) && costs.get(m.state) <= m.accumulatedCost)
                    continue;

                costs.put(m.state, m.accumulatedCost);
                open.add(m);
            }
        }

        boolean isOptimistic = true;
        for (String state : stateSpace.getStates()) {
            System.out.print("[CONDITION]: [");

            double estimatedCost = heuristic.getEstimatedCost(state);
            Double actualCost = costs.get(state);

            if (actualCost == null)
                actualCost = Double.POSITIVE_INFINITY;

            if (estimatedCost <= actualCost) {
                System.out.print("OK");
            } else {
                System.out.print("ERR");
                isOptimistic = false;
            }
            System.out.printf("] h(%s) <= h*: %.1f <= %.1f\n",
                    state, estimatedCost, actualCost);
        }

        if (isOptimistic)
            System.out.println("[CONCLUSION]: Heuristic is optimistic.");
        else
            System.out.println("[CONCLUSION]: Heuristic is not optimistic.");
    }

    public static void isConsistent(StateSpace stateSpace, Heuristic heuristic) {
        boolean isConsistent = true;
        for (String source : stateSpace.getStates()) {
            for (StateSpace.Successor s : stateSpace.getSuccessors(source)) {
                System.out.print("[CONDITION]: [");

                double estimatedCostSource = heuristic.getEstimatedCost(source);
                double estimatedCostDestination = heuristic.getEstimatedCost(s.destination);

                if (estimatedCostSource <= estimatedCostDestination + s.cost) {
                    System.out.print("OK");
                } else {
                    System.out.print("ERR");
                    isConsistent = false;
                }
                System.out.printf("] h(%s) <= h(%s) + c: %.1f <= %.1f + %.1f\n",
                        source, s.destination, estimatedCostSource, estimatedCostDestination, s.cost);
            }
        }

        if (isConsistent)
            System.out.println("[CONCLUSION]: Heuristic is consistent.");
        else
            System.out.println("[CONCLUSION]: Heuristic is not consistent.");
    }
}
