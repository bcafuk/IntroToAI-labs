package ui;

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
                    case "--alg":
                        if (arguments.operation != null)
                            throw new IllegalArgumentException("Multiple conflicting modes specified");
                        arguments.operation = Arguments.Operation.FIND_SOLUTION;

                        if (i + 1 >= args.length)
                            throw new IllegalArgumentException("--alg specified with no algowithm name");
                        i++;
                        arguments.algorithmIdentifier = switch (args[i]) {
                            case "bfs" -> AlgorithmIdentifier.BFS;
                            case "ucs" -> AlgorithmIdentifier.UCS;
                            case "astar" -> AlgorithmIdentifier.A_STAR;
                            default -> throw new IllegalArgumentException("Unknown algorithm " + args[i]);
                        };
                        break;
                    case "--check-optimistic":
                        if (arguments.operation != null)
                            throw new IllegalArgumentException("Multiple conflicting modes specified");
                        arguments.operation = Operation.CHECK_OPTIMISTIC;
                        break;
                    case "--check-consistent":
                        if (arguments.operation != null)
                            throw new IllegalArgumentException("Multiple conflicting modes specified");
                        arguments.operation = Operation.CHECK_CONSISTENT;
                        break;
                    case "--ss":
                        if (i + 1 >= args.length)
                            throw new IllegalArgumentException("--ss specified with no path");
                        i++;
                        arguments.stateSpacePath = args[i];
                        break;
                    case "--h":
                        if (i + 1 >= args.length)
                            throw new IllegalArgumentException("--h specified with no path");
                        i++;
                        arguments.heuristicPath = args[i];
                        break;
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
            BFS,
            UCS,
            A_STAR,
        }

        public enum Operation {
            FIND_SOLUTION,
            CHECK_OPTIMISTIC,
            CHECK_CONSISTENT,
        }
    }


}
