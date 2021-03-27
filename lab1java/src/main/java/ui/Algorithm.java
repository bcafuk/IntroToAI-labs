package ui;

import java.util.Objects;

@FunctionalInterface
public interface Algorithm {
    SearchResult search(StateSpace stateSpace, Heuristic heuristic);

    class SearchResult {
        public final Node node;
        public final int visitedCount;

        SearchResult(Node node, int visitedCount) {
            this.node = Objects.requireNonNull(node);
            this.visitedCount = visitedCount;
        }
    }
}
