package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Solution {

    public static void main(String... args) {
        String trainingFilename = args[0];
        String testingFilename = args[1];

        Dataset trainingData;
        Dataset testingData;
        try {
            trainingData = Dataset.parse(Files.lines(Path.of(trainingFilename)));
            testingData = Dataset.parse(Files.lines(Path.of(testingFilename)));
        } catch (IOException e) {
            System.err.println("Couldn't open file " + e.getMessage());
            System.exit(1);
            return;
        }

        if (!Dataset.labelsMatch(trainingData, testingData)) {
            System.err.println("The training and test sets' labels don't match");
            System.exit(1);
            return;
        }

        int classIndex = trainingData.getFeatureCount() - 1;

        ID3 id3;

        if (args.length > 2)
        	id3 = new ID3(Integer.parseInt(args[2]));
        else
        	id3 = new ID3();

        id3.fit(trainingData, classIndex);

        System.out.println("[BRANCHES]:");
        System.out.print(id3.toString());

        int correctPredictions = 0;
        Set<String> classLabels = new TreeSet<>();
        Map<ClassPair, Integer> confusion = new HashMap<>();

        System.out.print("[PREDICTIONS]:");
        for (String[] datum : testingData) {
            String prediction = id3.predict(datum);
            String expected = datum[classIndex];

            System.out.print(' ');
            System.out.print(prediction);

            if (prediction.equals(expected))
                correctPredictions++;

            classLabels.add(prediction);
            classLabels.add(expected);
            confusion.merge(new ClassPair(prediction, expected), 1, (f, dv) -> f + 1);
        }
        System.out.println();

        System.out.printf("[ACCURACY]: %.5f\n", (double)correctPredictions / testingData.numData());

        System.out.println("[CONFUSION_MATRIX]:");
        for (String expectedClass : classLabels) {
            for (String predictedClass : classLabels) {
                System.out.print(confusion.getOrDefault(new ClassPair(predictedClass, expectedClass), 0));
                System.out.print(' ');
            }
            System.out.println();
        }
    }

    private static class ClassPair {
        public String prediction;
        public String expected;

        public ClassPair(String prediction, String expected) {
            this.prediction = Objects.requireNonNull(prediction);
            this.expected = Objects.requireNonNull(expected);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClassPair classPair = (ClassPair) o;
            return prediction.equals(classPair.prediction) && expected.equals(classPair.expected);
        }

        @Override
        public int hashCode() {
            return Objects.hash(prediction, expected);
        }
    }
}
