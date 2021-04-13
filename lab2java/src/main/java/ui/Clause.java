package ui;

import java.util.*;
import java.util.stream.Collectors;

public class Clause {
    private final Map<String, Boolean> literals = new HashMap<>();

    public final Clause parent1;
    public final Clause parent2;

    public Clause() {
        this(null, null);
    }

    public Clause(Clause parent1, Clause parent2) {
        this.parent1 = parent1;
        this.parent2 = parent2;
    }

    public boolean isNil() {
        return literals.isEmpty();
    }

    public boolean subsumes(Clause that) {
        for (var entry : that.literals.entrySet())
            if (this.literals.get(entry.getKey()) != entry.getValue())
                return false;

        return true;
    }

    public Set<Clause> inverse() {
        Set<Clause> clauses = new HashSet<>();

        for (var entry : literals.entrySet()) {
            Clause clause = new Clause();
            clause.literals.put(entry.getKey(), !entry.getValue());
            clauses.add(clause);
        }

        return clauses;
    }

    public static Clause resolve(Clause a, Clause b) {
        Clause result = new Clause(a, b);

        result.literals.putAll(a.literals);

        boolean resolved = false;
        for (var bEntry : b.literals.entrySet()) {
            Boolean aValue = result.literals.get(bEntry.getKey());

            if (aValue == null) {
                result.literals.put(bEntry.getKey(), bEntry.getValue());
                continue;
            }

            if (aValue.equals(bEntry.getValue()))
                continue;

            if (resolved)
                return null;

            result.literals.remove(bEntry.getKey());
            resolved = true;
        }

        return resolved ? result : null;
    }

    public static Clause parse(String string) {
        Clause clause = new Clause();

        String[] split = string.toLowerCase().split("\\s");
        for (int i = 0; i < split.length; i++) {
            if (i % 2 == 1) {
                if (split[i].equals("v"))
                    continue;
                else
                    throw new IllegalArgumentException("Expected v, got " + split[i]);
            }

            boolean value = !split[i].startsWith("~");
            String atom = value ? split[i] : split[i].substring(1);

            Boolean prevValue = clause.literals.put(atom, value);
            if (prevValue != null && prevValue != value)
                return null;
        }


        return clause;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Clause clause = (Clause) o;
        return literals.equals(clause.literals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(literals);
    }

    @Override
    public String toString() {
        if (isNil())
            return "NIL";

        return literals.entrySet()
                       .stream()
                       .map(entry -> (entry.getValue() ? "" : "~") + entry.getKey())
                       .collect(Collectors.joining(" v "));
    }
}
