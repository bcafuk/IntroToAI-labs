package ui;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class StateSpace {
    private final Set<String> states = new TreeSet<>();
    private String initialState = null;
    private Set<String> goalStates = null;
    private final Map<String, Set<Successor>> successors = new HashMap<>();
    private final Map<String, Set<Successor>> predecessors = new HashMap<>();

    private StateSpace() {}

    public Set<String> getStates() {
        return Collections.unmodifiableSet(states);
    }

    public String getInitialState() {
        return initialState;
    }

    public boolean isGoalState(String state) {
        return goalStates.contains(Objects.requireNonNull(state));
    }

    public Set<String> getGoalStates() {
        return Collections.unmodifiableSet(goalStates);
    }

    public Set<Successor> getSuccessors(String state) {
        Set<Successor> set = successors.get(Objects.requireNonNull(state));
        if (set != null)
            return Collections.unmodifiableSet(set);
        else
            return Collections.emptySet();
    }

    public Set<Successor> getPredecessors(String state) {
        Set<Successor> set = predecessors.get(Objects.requireNonNull(state));
        if (set != null)
            return Collections.unmodifiableSet(set);
        else
            return Collections.emptySet();
    }

    private void addSuccessor(String from, String to, double cost) {
        if (this.successors.containsKey(from)) {
            this.successors.get(from).add(new Successor(to, cost));
        } else {
            Set<Successor> successorSet = new TreeSet<>();
            successorSet.add(new Successor(to, cost));
            this.successors.put(from, successorSet);
        }

        if (this.predecessors.containsKey(to)) {
            this.predecessors.get(to).add(new Successor(from, cost));
        } else {
            Set<Successor> predecessorSet = new TreeSet<>();
            predecessorSet.add(new Successor(from, cost));
            this.predecessors.put(to, predecessorSet);
        }
    }

    public static StateSpace read(Stream<String> lines) throws IOException {
        StateSpace ss = new StateSpace();

        lines.forEachOrdered(line -> {
            if (line.startsWith("#"))
                return;

            if (ss.initialState == null) {
                ss.initialState = line;
                return;
            }

            if (ss.goalStates == null) {
                ss.goalStates = new HashSet<>(Arrays.asList(line.split(" ")));
                return;
            }

            int colonPosition = line.indexOf(':');
            String from = line.substring(0, colonPosition);

            ss.states.add(from);

            if (colonPosition + 2 >= line.length())
                return;

            String successors = line.substring(colonPosition + 2);
            Arrays.stream(successors.split(" ")).forEach(succ -> {
                int commaPosition = succ.indexOf(',');
                String to = succ.substring(0, commaPosition);
                double cost = Double.parseDouble(succ.substring(commaPosition + 1));

                ss.addSuccessor(from, to, cost);
            });
        });

        return ss;
    }

    public static final class Successor implements Comparable<Successor> {
        public final String destination;
        public final double cost;

        public Successor(String destination, double cost) {
            this.destination = Objects.requireNonNull(destination);
            this.cost = cost;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Successor successor = (Successor) o;
            return destination.equals(successor.destination);
        }

        @Override
        public int hashCode() {
            return Objects.hash(destination);
        }

        @Override
        public int compareTo(Successor o) {
            return destination.compareTo(o.destination);
        }
    }
}
