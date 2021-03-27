package ui;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

public class Heuristic {
    private final Map<String, Double> estimatedCosts = new HashMap<>();

    public double getEstimatedCost(String state) {
        return estimatedCosts.get(state);
    }

    public static Heuristic read(Stream<String> lines) throws IOException {
        Heuristic h = new Heuristic();

        lines.forEachOrdered(line -> {
            if (line.startsWith("#"))
                return;

            int colonPosition = line.indexOf(':');
            String state = line.substring(0, colonPosition);
            double estimatedCost = Double.parseDouble(line.substring(colonPosition + 2));

            h.estimatedCosts.put(state, estimatedCost);
        });

        return h;
    }
}
