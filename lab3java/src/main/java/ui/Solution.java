package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

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

        System.out.print("[PREDICTIONS]:");
        for (String[] datum : testingData) {
            System.out.print(' ');
            System.out.print(id3.predict(datum));
        }
        System.out.println();
    }

}
