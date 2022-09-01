package org.diceresearch.factchecktrainer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.aksw.defacto.ml.training.TrainingClassifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FactcheckTrainerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(FactcheckTrainerApplication.class, args);
	}

	@Override
	public void run(String... args) throws IOException {

		if (args == null || args.length == 0) {
			System.out.println("no arguments ! use h to get help");
			return;
		}
		if (args.length ==  1 && args[0].equals("h")) {
			System.out.println("this is help");
			System.out.println("1 . use 'train' for train the classifier ");
			System.out.println("\t \t train [full path for save classifier] [full path for save evaluation] [full path to training file] [classifier one of these options: MultilayerPerceptron, J48, LibSVM] [comma separated index of the training data for delete like this : 29,27,5]");
		}

		System.out.println("args len is  " + args.length);

		for(String a:args){
			System.out.println(a);
		}

		if (args.length == 6 && args[0].equals("train")){
			TrainingClassifier c = new TrainingClassifier();
			String pathToClassifier = args[1];
			String pathToEvaluation = args[2] ;
			String pathToTrainingData = args[3];
			String classifier = args[4];
			String[] indices = args[5].split(",");
			List<Integer> tempIndicesArral = new ArrayList<>();
			for(String number:indices){
				tempIndicesArral.add(Integer.parseInt(number.trim()));
			}
			int[] inicesToRemove = new int[tempIndicesArral.size()];
			for(int i = 0 ; i < tempIndicesArral.size() ; i++){
				inicesToRemove[i] = tempIndicesArral.get(i);
			}

			c.train(TrainingClassifier.classifierName.valueOf(classifier), pathToClassifier,pathToEvaluation,pathToTrainingData,inicesToRemove);

			System.out.println("done");
			System.exit(0);
		}
	}
}
