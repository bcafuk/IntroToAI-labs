package ui;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Dataset implements Iterable<String[]> {
    private static final double LOG_2 = Math.log(2.0);

    private final String[] labels;
    private final List<String[]> data;

    private Dataset(String[] labels) {
        this(labels, new ArrayList<>());
    }

    private Dataset(String[] labels, List<String[]> data) {
        this.labels = Objects.requireNonNull(labels);
        this.data = Objects.requireNonNull(data);

        for (String[] datum : data)
            if (datum.length != labels.length)
                throw new IllegalArgumentException("Length mismatch: a row contains " + datum.length + " values, but " + labels.length + " were expected.");
    }

    private void addDatum(String[] datum) {
        Objects.requireNonNull(datum);
        if (datum.length != labels.length)
            throw new IllegalArgumentException("Length mismatch: the datum contains " + datum.length + " values, but " + labels.length + " were expected.");

        data.add(datum);
    }

    public boolean isEmpty() {
        return data.isEmpty();
    }

    public int numData() {
        return data.size();
    }

    public int getFeatureCount() {
        return labels.length;
    }

    public String getLabelForIndex(int index) {
        return labels[index];
    }

    public int getIndexForLabel(String label) {
        Objects.requireNonNull(label);

        for (int i = 0; i < labels.length; i++)
            if (labels[i].equals(label))
                return i;

        throw new NoSuchElementException("The dataset does not contain the label " + label);
    }

    public String mostFrequentFor(int featureIndex) {
        Map<String, Integer> frequencies = new TreeMap<>();

        for (String[] datum : data)
            frequencies.merge(datum[featureIndex], 1, (f, df) -> f + 1);

        return frequencies.entrySet()
                          .stream()
                          .max(Map.Entry.comparingByValue())
                          .orElseThrow()
                          .getKey();
    }

    public Map<String, Dataset> splitBy(int featureIndex) {
        Map<String, Dataset> subsets = new TreeMap<>();

        for (String[] datum : data)
            subsets.computeIfAbsent(datum[featureIndex], v -> new Dataset(labels))
                   .addDatum(datum);

        return subsets;
    }

    public Map<Integer, Double> informationGain(Set<Integer> featureIndices, int classIndex) {
        Map<String, Integer> totalClassFrequencies = new HashMap<>();
        Map<Integer, Map<String, Map<String, Integer>>> classFrequencies = new TreeMap<>();

        for (int featureIndex : featureIndices)
            classFrequencies.put(featureIndex, new HashMap<>());

        for (String[] datum : data) {
            totalClassFrequencies.merge(datum[classIndex], 1, (f, df) -> f + 1);

            for (int featureIndex : featureIndices)
                classFrequencies.get(featureIndex)
                                .computeIfAbsent(datum[featureIndex], k -> new HashMap<>())
                                .merge(datum[classIndex], 1, (f, df) -> f + 1);
        }

        double totalEntropy = getEntropyForFrequencies(totalClassFrequencies);

        Map<Integer, Double> gains = new HashMap<>();

        for (int featureIndex : featureIndices) {
            double gain = totalEntropy;

            for (Map<String, Integer> frequencies : classFrequencies.get(featureIndex).values()) {
                int count = getTotalCount(frequencies);
                gain -= (double) count / numData() * getEntropyForFrequencies(frequencies, count);
            }

            gains.put(featureIndex, gain);
        }

        return gains;
    }

    private int getTotalCount(Map<String, Integer> frequencies) {
        return frequencies.values()
                          .stream()
                          .mapToInt(c -> c)
                          .sum();
    }

    private double getEntropyForFrequencies(Map<String, Integer> frequencies) {
        return getEntropyForFrequencies(frequencies, getTotalCount(frequencies));
    }

    private double getEntropyForFrequencies(Map<String, Integer> frequencies, int totalCount) {
        return -frequencies.values()
                           .stream()
                           .mapToDouble(c -> {
                               double p = (double) c / totalCount;
                               return p * Math.log(p) / LOG_2;
                           })
                           .sum();
    }

    public static Dataset parse(Stream<String> lines) {
        Objects.requireNonNull(lines);

        LinkedList<String[]> data = lines.map(line -> line.split(","))
                                         .collect(Collectors.toCollection(LinkedList::new));

        String[] labels = data.remove(0);


        return new Dataset(labels, data);
    }

    public static boolean labelsMatch(Dataset d1, Dataset d2) {
        return Arrays.equals(d1.labels, d2.labels);
    }

    @Override
    public Iterator<String[]> iterator() {
        return data.iterator();
    }
}
