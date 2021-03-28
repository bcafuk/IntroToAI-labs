package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class Solution {
    private static final String USAGE_STRING = """
            Usage:
                java ui.Solution --alg algorithm --ss path_to_state_space [--h path_to_heuristic]
                    Finds a solution in the state space.
                    Supported algorithms:
                        bfs   Breadth-first search
                        ucs   Uniform-cost search
                        astar A* search; requires a heuristic
                java ui.Solution --check-optimistic --ss path_to_state_space --h path_to_heuristic
                    Checks whether a heuristic is optimistic for the given state space.
                java ui.Solution --check-consistent --ss path_to_state_space --h path_to_heuristic
                    Checks whether a heuristic is consistent for the given state space.
            """;

    public static void main(String... args) {
        Arguments arguments;
        try {
            arguments = Arguments.parseArguments(args);
        } catch (IllegalArgumentException e) {
            System.err.println("Command line argument error: " + e.getMessage());
            System.err.println(USAGE_STRING);

            System.exit(1);
            return;
        }

        StateSpace stateSpace;
        try {
            stateSpace = StateSpace.read(Files.lines(Path.of(arguments.stateSpacePath)));
        } catch (IOException e) {
            System.err.println("Error reading state space descriptor from path " + arguments.stateSpacePath);

            System.exit(1);
            return;
        }

        Heuristic heuristic = null;
        if (arguments.heuristicPath != null) {
            try {
                heuristic = Heuristic.read(Files.lines(Path.of(arguments.heuristicPath)));
            } catch (IOException e) {
                System.err.println("Error reading heuristic descriptor from path " + arguments.heuristicPath);

                System.exit(1);
                return;
            }
        }

        switch (arguments.operation) {
            case FIND_SOLUTION -> {
                SearchAlgorithm.SearchResult searchResult = (switch (arguments.algorithmIdentifier) {
                    case BFS -> SearchAlgorithms.BREADTH_FIRST_SEARCH;
                    case UCS -> SearchAlgorithms.UNIFORM_COST_SEARCH;
                    case A_STAR -> SearchAlgorithms.A_STAR_SEARCH;
                }).search(stateSpace, heuristic);

                if (heuristic != null) {
                    System.out.println("# " + arguments.algorithmIdentifier.friendlyName + " " + arguments.heuristicPath);
                } else {
                    System.out.println("# " + arguments.algorithmIdentifier.friendlyName);
                }
                System.out.println("[FOUND_SOLUTION]: " + (searchResult == null ? "no" : "yes"));

                if (searchResult != null) {
                    List<String> path = new LinkedList<>();

                    Node n = searchResult.node;
                    while (n != null) {
                        path.add(0, n.state);
                        n = n.parent;
                    }

                    System.out.println("[STATES_VISITED]: " + searchResult.visitedCount);
                    System.out.println("[PATH_LENGTH]: " + path.size());
                    System.out.printf("[TOTAL_COST]: %.1f\n", searchResult.node.accumulatedCost);
                    System.out.println("[PATH]: " + String.join(" => ", path));
                }
            }
            case CHECK_OPTIMISTIC -> {
                System.out.println("# HEURISTIC-OPTIMISTIC " + arguments.heuristicPath);
                Checks.isOptimistic(stateSpace, heuristic);
            }
            case CHECK_CONSISTENT -> {
                System.out.println("# HEURISTIC-CONSISTENT " + arguments.heuristicPath);
                Checks.isConsistent(stateSpace, heuristic);
            }
        }
    }

    private static class Arguments {
        public Operation operation = null;
        public AlgorithmIdentifier algorithmIdentifier = null;
        public String stateSpacePath = null;
        public String heuristicPath = null;

        private static Arguments parseArguments(String[] args) {
            Arguments arguments = new Arguments();

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--alg" -> {
                        if (arguments.operation != null)
                            throw new IllegalArgumentException("Multiple conflicting modes specified");
                        arguments.operation = Operation.FIND_SOLUTION;
                        if (i + 1 >= args.length)
                            throw new IllegalArgumentException("--alg specified with no algowithm name");
                        i++;
                        arguments.algorithmIdentifier = switch (args[i]) {
                            case "bfs" -> AlgorithmIdentifier.BFS;
                            case "ucs" -> AlgorithmIdentifier.UCS;
                            case "astar" -> AlgorithmIdentifier.A_STAR;
                            default -> throw new IllegalArgumentException("Unknown algorithm " + args[i]);
                        };
                    }
                    case "--check-optimistic" -> {
                        if (arguments.operation != null)
                            throw new IllegalArgumentException("Multiple conflicting modes specified");
                        arguments.operation = Operation.CHECK_OPTIMISTIC;
                    }
                    case "--check-consistent" -> {
                        if (arguments.operation != null)
                            throw new IllegalArgumentException("Multiple conflicting modes specified");
                        arguments.operation = Operation.CHECK_CONSISTENT;
                    }
                    case "--ss" -> {
                        if (i + 1 >= args.length)
                            throw new IllegalArgumentException("--ss specified with no path");
                        i++;
                        arguments.stateSpacePath = args[i];
                    }
                    case "--h" -> {
                        if (i + 1 >= args.length)
                            throw new IllegalArgumentException("--h specified with no path");
                        i++;
                        arguments.heuristicPath = args[i];
                    }
                }
            }

            if (arguments.operation == null)
                throw new IllegalArgumentException("Missing mode of operation");
            if (arguments.algorithmIdentifier == null && arguments.operation == Operation.FIND_SOLUTION)
                throw new IllegalArgumentException("Missing algorithm name");
            if (arguments.stateSpacePath == null)
                throw new IllegalArgumentException("Missing state space path");
            if (arguments.heuristicPath == null && (arguments.operation != Operation.FIND_SOLUTION ||
                    arguments.algorithmIdentifier == AlgorithmIdentifier.A_STAR))
                throw new IllegalArgumentException("Missing heuristic path");
            if (arguments.heuristicPath != null && arguments.operation == Operation.FIND_SOLUTION &&
                    arguments.algorithmIdentifier != AlgorithmIdentifier.A_STAR)
                throw new IllegalArgumentException("This mode or algorithm does not allow a heuristic");

            return arguments;
        }

        public enum AlgorithmIdentifier {
            BFS("BFS"),
            UCS("UCS"),
            A_STAR("A-STAR");

            public String friendlyName;

            AlgorithmIdentifier(String friendlyName) {
                this.friendlyName = friendlyName;
            }
        }

        public enum Operation {
            FIND_SOLUTION,
            CHECK_OPTIMISTIC,
            CHECK_CONSISTENT,
        }
    }


}
