package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Solution {
    public static void main(String... args) {
        String mode = args[0];

        try {
            switch (mode) {
                case "resolution" -> resolution(args[1]);
                case "cooking" -> cooking(args[1], args[2]);
                default -> throw new IllegalArgumentException("Unknown mode " + mode);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
    }

    private static void resolution(String clauseFile) throws IOException {
        List<String> lines = Files.lines(Path.of(clauseFile))
                                  .filter(line -> !line.startsWith("#"))
                                  .collect(Collectors.toList());

        String goalText = lines.remove(lines.size() - 1);

        List<Clause> clauses = lines.stream()
                                    .map(Clause::parse)
                                    .filter(Objects::nonNull)
                                    .collect(Collectors.toList());
        Clause goal = Clause.parse(goalText);

        Clause result = RefutationResolver.resolution(clauses, goal);

        if (result != null)
            printClauseTrace(result, clauses, goal);

        System.out.print("[CONCLUSION]: " + goalText.toLowerCase() + " is ");
        if (result == null)
            System.out.println("unknown");
        else
            System.out.println("true");
    }

    private static void cooking(String clauseFile, String commandFile) throws IOException {
        Set<Clause> clauses = Files.lines(Path.of(clauseFile))
                                   .filter(line -> !line.startsWith("#"))
                                   .map(Clause::parse)
                                   .filter(Objects::nonNull)
                                   .collect(Collectors.toCollection(LinkedHashSet::new));

        Files.lines(Path.of(commandFile))
             .forEachOrdered(line -> {
                 char command = line.charAt(line.length() - 1);
                 String clauseText = line.substring(0, line.length() - 2);
                 Clause clause = Clause.parse(clauseText);

                 switch (command) {
                     case '+' -> {
                         if (clause != null)
                             clauses.add(clause);
                     }
                     case '-' -> clauses.remove(clause);
                     case '?' -> {
                         Clause result = RefutationResolver.resolution(clauses, clause);

                         System.out.println();
                         if (result != null)
                             printClauseTrace(result, clauses, clause);

                         System.out.print("[CONCLUSION]: " + clauseText.toLowerCase() + " is ");
                         if (result == null)
                             System.out.println("unknown");
                         else
                             System.out.println("true");
                     }
                     default -> throw new RuntimeException("Illegal command " + command);
                 }
             });
    }

    public static void printClauseTrace(Clause clause, Collection<Clause> premises, Clause goal) {
        Set<Clause> goalInverse = goal == null ? Set.of(new Clause()) : goal.inverse();

        LinkedHashSet<Clause> clauseSet = new LinkedHashSet<>();
        traverseIndices(clause, clauseSet);

        List<Clause> clauseList = new ArrayList<>(clauseSet);
        Collections.reverse(clauseList);

        clauseList.sort((a, b) -> {
            boolean aIsPremise = premises.contains(a);
            boolean bIsPremise = premises.contains(b);

            if (aIsPremise != bIsPremise)
                return (bIsPremise ? 1 : 0) - (aIsPremise ? 1 : 0);

            boolean aIsGoal = goalInverse.contains(a);
            boolean bIsGoal = goalInverse.contains(b);

            if (aIsGoal != bIsGoal)
                return (bIsGoal ? 1 : 0) - (aIsGoal ? 1 : 0);

            return 0;
        });

        for (int i = 0; i < clauseList.size(); i++) {
            Clause c = clauseList.get(i);

            boolean isRootClause = c.parent1 == null && c.parent2 == null;

            System.out.print(i);
            System.out.print(". ");
            System.out.print(c.toString());

            if (!isRootClause) {
                System.out.print(" (");
                System.out.print(clauseList.indexOf(c.parent2));
                System.out.print(", ");
                System.out.print(clauseList.indexOf(c.parent1));
                System.out.print(")");
            }

            System.out.println();
        }
    }

    private static void traverseIndices(Clause clause, Set<Clause> clauses) {
        clauses.add(clause);

        boolean isRootClause = clause.parent1 == null && clause.parent2 == null;

        if (!isRootClause) {
            traverseIndices(clause.parent1, clauses);
            traverseIndices(clause.parent2, clauses);
        }
    }
}
