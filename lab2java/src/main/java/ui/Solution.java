package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
                clauses.addAll(goal.inverse());
            }
            case "cookbook" -> {
                String clauseFile = args[1];
                String commandFile = args[2];

                throw new RuntimeException("Not yet implemented");
            }
            default -> throw new IllegalArgumentException("Unknown mode " + mode);
        }
    }

    private static List<Clause> parseClauses(Stream<String> lines) {
        return lines.filter(line -> !line.startsWith("#"))
                    .map(Clause::parse)
                    .collect(Collectors.toList());
    }
}
