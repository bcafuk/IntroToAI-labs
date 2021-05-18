package ui;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Solution {

	public static void main(String ... args) {
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
	}

}
