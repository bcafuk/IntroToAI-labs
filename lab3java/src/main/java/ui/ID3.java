package ui;

import java.util.*;

public class ID3 {
    private int classIndex = -1;
    private Dataset dataset = null;
    private TreeNode root = null;

    public void fit(Dataset dataset, String classLabel) {
        classIndex = dataset.getIndexForLabel(classLabel);
        this.dataset = dataset;

        Set<Integer> featureSet = new HashSet<>();
        for (int i = 0; i < dataset.getFeatureCount(); i++)
            if (i != classIndex)
                featureSet.add(i);

        root = id3(dataset, dataset, featureSet);
    }

    private TreeNode id3(Dataset subset, Dataset parent, Set<Integer> featureSet) {
        // TODO: Implement ID3
        return null;
    }

    public String predict(String[] datum) {
        if (root == null)
            throw new IllegalStateException("The model has not yet been fitted.");

        return root.predict(datum);
    }

    private interface TreeNode {
        String predict(String[] datum);
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
    }
}
