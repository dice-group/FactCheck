package org.aksw.defacto.ml.training;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.*;
import java.util.Random;

// pathToClassifier is path for saving the model
// pathToEvaluation is path for save the evaluation
// pathToTrainingData for train the model


public class ClassifierModelTrainer {
    private String pathToClassifier;// = "/home/farshad/experiments/Trainfactchek/Fact/finaltraining/allPredicates/model/1MultilayerPerceptronClassifier.model";//    = new File(FactScorer.class.getResource("/classifier/evidence/" + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE") + ".model").getFile()).getAbsolutePath();
    private String pathToEvaluation;//     ="/home/farshad/experiments/Trainfactchek/Fact/finaltraining/allPredicates/model/_1positivNegativMultilayerPerceptron.arff.eval.model" ;//DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE") + ".eval.model";
    private String pathToTrainingData;//   = "/home/farshad/experiments/Trainfactchek/Fact/finaltraining/allPredicates/_1positivNegativ.arff";//DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_TRAINING_DATA_FILENAME");
    private Classifier classifier;
    private int[] removeFilterAttributes;
    public ClassifierModelTrainer(String pathToClassifier, String pathToEvaluation, String pathToTrainingData, Classifier classifier, int[] removeFilterAttributes) {
        this.pathToClassifier = pathToClassifier;
        this.pathToEvaluation = pathToEvaluation;
        this.pathToTrainingData = pathToTrainingData;
        this.classifier = classifier;
        this.removeFilterAttributes = removeFilterAttributes;
    }

    public void trainClassifier() {

        String errorMessage = "Could not train classifier from: " + pathToClassifier + " with the training file: " + this.pathToTrainingData;

        try {


            // train
            Instances inst = new Instances(new BufferedReader(new FileReader(pathToTrainingData)));


            Remove removeFilter = new Remove();
            removeFilter.setAttributeIndicesArray( removeFilterAttributes);
            removeFilter.setInputFormat(inst);
            Instances newData = Filter.useFilter(inst, removeFilter);
            newData.setClassIndex(newData.numAttributes() - 1);
            classifier.buildClassifier(newData);

            // eval
            Evaluation evaluation = new Evaluation(newData);
            evaluation.crossValidateModel(classifier, newData, 2, new Random(1));

            // write eval
            FileOutputStream fos = new FileOutputStream(pathToEvaluation);
            fos.write(evaluation.toSummaryString().getBytes());
            fos.flush();
            fos.close();

            // serialize model
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pathToClassifier));
            oos.writeObject(classifier);
            oos.flush();
            oos.close();

        }
        catch (FileNotFoundException e) {

            throw new RuntimeException(errorMessage, e);
        }
        catch (IOException e) {

            throw new RuntimeException(errorMessage, e);
        }
        catch (Exception e) {

            throw new RuntimeException(errorMessage, e);
        }
    }
}
