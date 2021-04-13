package ui;

import java.util.*;

public class RefutationResolver {
    static Clause resolution(List<Clause> inputClauses, Clause goal) {
        List<Clause> clauses = new ArrayList<>(inputClauses);
        Set<Integer> support = new HashSet<>();

        Set<IntPair> openedPairs = new HashSet<>();
        Set<Clause> opened = new HashSet<>();
        Queue<Integer> open = new LinkedList<>();

        clauses.addAll(goal.inverse());
        for (int i = inputClauses.size(); i < clauses.size(); i++) {
            support.add(i);
            open.add(i);
        }

        while (!open.isEmpty()) {
            int clause1 = open.poll();

            for (int clause2 = 0, clauseCount = clauses.size(); clause2 < clauseCount; clause2++) {
                if (clause1 == clause2)
                    continue;

                IntPair clausePair = new IntPair(clause1, clause2);

                if (!openedPairs.add(clausePair))
                    continue;

                Clause newClause = Clause.resolve(clauses.get(clause1), clauses.get(clause2));
                if (!opened.add(newClause))
                    continue;

                if (newClause == null)
                    continue;
                if (newClause.isNil())
                    return newClause;

                int newClauseIndex = clauses.size();
                support.add(newClauseIndex);
                open.add(newClauseIndex);
                clauses.add(newClause);
            }
        }

        return null;
    }

    private static class IntPair {
        public final int a;
        public final int b;

        public IntPair(int a, int b) {
            if (a < b) {
                this.a = a;
                this.b = b;
            } else {
                this.a = b;
                this.b = a;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            IntPair intPair = (IntPair) o;
            return a == intPair.a && b == intPair.b;
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }
    }
}
