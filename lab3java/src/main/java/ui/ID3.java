package ui;

import java.util.*;

public class ID3 {
    private final int maxDepth;

    private int classIndex = -1;
    private Dataset dataset = null;
    private TreeNode root = null;

    public ID3() {
        this(Integer.MAX_VALUE);
    }

    public ID3(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public void fit(Dataset dataset, int classIndex) {
        this.classIndex = classIndex;
        this.dataset = dataset;

        Set<Integer> featureSet = new HashSet<>();
        for (int i = 0; i < dataset.getFeatureCount(); i++)
            if (i != classIndex)
                featureSet.add(i);

        root = id3(dataset, dataset, featureSet, 0);
    }

    private TreeNode id3(Dataset subset, Dataset parent, Set<Integer> featureSet, int depth) {
        if (subset.isEmpty())
            return new Leaf(parent.mostFrequentFor(classIndex));

        if (depth >= maxDepth || featureSet.isEmpty() || subset.distinctValueCount(classIndex) == 1)
            return new Leaf(subset.mostFrequentFor(classIndex));

        Map<Integer, Double> informationGains = subset.informationGain(featureSet, classIndex);

        double maxInformationGain = informationGains.values()
                                                    .stream()
                                                    .max(Comparator.naturalOrder())
                                                    .orElseThrow();
        int featureIndex = informationGains.entrySet()
                                           .stream()
                                           .filter(e -> e.getValue().equals(maxInformationGain))
                                           .map(Map.Entry::getKey)
                                           .min(Comparator.comparing(subset::getLabelForIndex))
                                           .orElseThrow();

        Node node = new Node(featureIndex, subset);

        Map<String, Dataset> subSubsets = subset.splitBy(featureIndex);
        for (Map.Entry<String, Dataset> subSubset : subSubsets.entrySet()) {
            Set<Integer> newFeatureSet = new HashSet<>(featureSet);
            newFeatureSet.remove(featureIndex);

            TreeNode subtree = id3(subSubset.getValue(), subset, newFeatureSet, depth + 1);
            node.append(subSubset.getKey(), subtree);
        }

        return node;
    }

    public String predict(String[] datum) {
        if (root == null)
            throw new IllegalStateException("The model has not yet been fitted.");

        return root.predict(datum);
    }

    @Override
    public String toString() {
        return root.buildString(new StringBuilder(), 1).toString();
    }

    private interface TreeNode {
        String predict(String[] datum);

        StringBuilder buildString(StringBuilder prefix, int level);
    }

    private class Node implements TreeNode {
        private final int featureIndex;
        private final Map<String, TreeNode> subtrees = new HashMap<>();
        private final Dataset subset;

        public Node(int featureIndex, Dataset subset) {
            this.featureIndex = featureIndex;
            this.subset = subset;
        }

        public void append(String featureValue, TreeNode subtree) {
            subtrees.put(Objects.requireNonNull(featureValue), Objects.requireNonNull(subtree));
        }

        @Override
        public String predict(String[] datum) {
            String featureValue = datum[featureIndex];
            TreeNode subtree = subtrees.get(featureValue);

            if (subtree != null)
                return subtree.predict(datum);
            else
                return subset.mostFrequentFor(classIndex);
        }

        @Override
        public StringBuilder buildString(StringBuilder prefix, int level) {
            String featureLabel = dataset.getLabelForIndex(featureIndex);
            StringBuilder sb = new StringBuilder();

            for (Map.Entry<String, TreeNode> subtree : subtrees.entrySet()) {
                StringBuilder pb = new StringBuilder(prefix)
                        .append(level)
                        .append(':')
                        .append(featureLabel)
                        .append('=')
                        .append(subtree.getKey())
                        .append(' ');

                sb.append(subtree.getValue().buildString(pb, level + 1));
            }

            return sb;
        }
    }

    private static class Leaf implements TreeNode {
        private final String predictedClass;

        public Leaf(String predictedClass) {
            this.predictedClass = Objects.requireNonNull(predictedClass);
        }

        @Override
        public String predict(String[] datum) {
            return predictedClass;
        }

        @Override
        public StringBuilder buildString(StringBuilder prefix, int level) {
            return new StringBuilder(prefix)
                    .append(predictedClass)
                    .append('\n');
        }
    }
}
