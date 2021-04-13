package ui;

import java.util.*;

public class RefutationResolver {
    static Clause resolution(Collection<Clause> inputClauses, Clause goal) {
        if (goal == null)
            return new Clause();

        Set<Clause> goalInverse = goal.inverse();

        Set<Clause> clauses = nonReduntant(inputClauses);
        clauses.addAll(goalInverse);

        Set<Clause> opened = new HashSet<>(goalInverse);
        Queue<Clause> open = new LinkedList<>(goalInverse);

        while (!open.isEmpty()) {
            Clause clause1 = open.poll();

            Set<Clause> newClauses = new HashSet<>();

            for (Clause clause2 : clauses) {
                if (clause1 == clause2)
                    continue;

                Clause newClause = Clause.resolve(clause1, clause2);
                if (newClause == null)
                    continue;
                if (newClause.isNil())
                    return newClause;

                if (opened.add(newClause))
                    newClauses.add(newClause);
            }

            for (Clause newClause : newClauses) {
                boolean isSubsumed = false;

                for (Iterator<Clause> existingIt = clauses.iterator(); existingIt.hasNext(); ) {
                    Clause existing = existingIt.next();

                    if (existing.subsumes(newClause)) {
                        isSubsumed = true;
                        break;
                    }

                    if (newClause.subsumes(existing)) {
                        existingIt.remove();
                        open.removeIf(c -> c == existing);
                    }
                }

                if (!isSubsumed) {
                    clauses.add(newClause);
                    open.add(newClause);
                }
            }
        }

        return null;
    }

    private static Set<Clause> nonReduntant(Collection<Clause> input) {
        Set<Clause> output = new HashSet<>();

        for (Clause a : input) {
            boolean isSubsumed = false;

            for (Clause b : input) {
                if (a != b && b.subsumes(a)) {
                    isSubsumed = true;
                    break;
                }
            }

            if (!isSubsumed)
                output.add(a);
        }

        return output;
    }
}
