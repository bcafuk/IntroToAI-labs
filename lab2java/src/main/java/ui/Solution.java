package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Solution {
    public static void main(String... args) {
        String mode = args[0];

        switch (mode) {
            case "resolution" -> {
                String clauseFile = args[1];

                List<Clause> clauses;

                try {
                    clauses = parseClauses(Files.lines(Path.of(clauseFile)));
                } catch (IOException e) {
                    System.err.println("Error reading clauses from path " + clauseFile);

                    System.exit(1);
                    return;
                }

                Clause goal = clauses.remove(clauses.size() - 1);

                Clause result = RefutationResolver.resolution(clauses, goal);

                System.out.println(clauseTrace(result));

                System.out.print("[CONCLUSION]: " + goal.toString() + " is ");
                if (result == null)
                    System.out.println("unknown");
                else
                    System.out.println("true");
            }
            case "cookbook" -> {
                String clauseFile = args[1];
                String commandFile = args[2];

                throw new RuntimeException("Not yet implemented");
            }
            default -> throw new IllegalArgumentException("Unknown mode " + mode);
        }
    }

    public static List<Clause> parseClauses(Stream<String> lines) {
        return lines.filter(line -> !line.startsWith("#"))
                    .map(Clause::parse)
                    .collect(Collectors.toList());
    }

    public static String clauseTrace(Clause clause) {
        if (clause == null)
            return "";

        LinkedHashMap<Clause, Integer> indices = new LinkedHashMap<>();
        traverseIndices(clause, indices);

        List<String> lines = new LinkedList<>();

        for (var entry : indices.entrySet()) {
            boolean isRootClause = entry.getKey().parent1 == null && entry.getKey().parent2 == null;

            StringBuilder line = new StringBuilder();
            line.append(entry.getValue())
                .append(". ")
                .append(entry.getKey().toString());

            if (!isRootClause) {
                line.append(" (")
                    .append(indices.get(entry.getKey().parent1))
                    .append(", ")
                    .append(indices.get(entry.getKey().parent2))
                    .append(")");
            }

            lines.add(0, line.toString());
        }

        return String.join("\n", lines);
    }

    private static void traverseIndices(Clause clause, Map<Clause, Integer> indices) {
        int index = indices.size();
        indices.put(clause, index);

        boolean isRootClause = clause.parent1 == null && clause.parent2 == null;

        if (!isRootClause) {
            traverseIndices(clause.parent1, indices);
            traverseIndices(clause.parent2, indices);
        }
    }
}
