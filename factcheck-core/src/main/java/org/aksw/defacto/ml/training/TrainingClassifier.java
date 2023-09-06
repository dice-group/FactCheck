package org.aksw.defacto.ml.training;

import weka.classifiers.Classifier;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.J48;

public class TrainingClassifier {

    public enum classifierName {
        MultilayerPerceptron,
        J48,
        LibSVM,
        SMO
    }

    public TrainingClassifier() {
    }

    public void train(classifierName cn, String pathToClassifier, String pathToEvaluation, String pathToTrainingData,int[] removeFilterAttributes){

        Classifier classifier = new MultilayerPerceptron();
        System.out.println("the name of classifier which announced bu user is :"+cn.name());
        switch (cn){
            case J48:
                classifier = new J48();
                break;
            case LibSVM:
                classifier = new LibSVM();
                break;
            case MultilayerPerceptron:
                classifier = new MultilayerPerceptron();
                break;
            case SMO:
                classifier = new SMO();
                break;
        }

        ClassifierModelTrainer factTrainer = new ClassifierModelTrainer(pathToClassifier, pathToEvaluation, pathToTrainingData, classifier, removeFilterAttributes);
        factTrainer.trainClassifier();
    }
/*
    public void train1(){
        Classifier classifier;
        String pathToClassifier = "/home/farshad/experiments/Trainfactchek/Fact/finaltraining/allPredicates/model/1MultilayerPerceptronClassifier.model";
        String pathToEvaluation ="/home/farshad/experiments/Trainfactchek/Fact/finaltraining/allPredicates/model/_1positivNegativMultilayerPerceptron.arff.eval.model" ;
        String pathToTrainingData = "/home/farshad/experiments/Trainfactchek/Fact/finaltraining/allPredicates/_1positivNegativ.arff";

        classifier = new MultilayerPerceptron();
        int[] tt = {29,28,27,26,25};
        ClassifierModelTrainer factTrainer = new ClassifierModelTrainer(pathToClassifier, pathToEvaluation, pathToTrainingData, classifier, tt);
        factTrainer.trainClassifier();
    }

    public void train2(){
        Classifier classifier;
        String pathToClassifier = "/home/farshad/experiments/Trainfactchek/Fact/finaltraining/allPredicates/model/2J48PositveTrimmNegativLittleTrim2.model";
        String pathToEvaluation     ="/home/farshad/experiments/Trainfactchek/Fact/finaltraining/allPredicates/model/_2positivTrimmNegativLittleTrimm2.arff.eval.model" ;
        String pathToTrainingData   = "/home/farshad/experiments/Trainfactchek/Fact/finaltraining/allPredicates/_2positivTrimmNegativLittleTrimm.arff";

        classifier = new J48();
        int[] tt = {0};
        ClassifierModelTrainer factTrainer = new ClassifierModelTrainer(pathToClassifier, pathToEvaluation, pathToTrainingData, classifier, tt);
        factTrainer.trainClassifier();
    }*/
}
