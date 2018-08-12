package org.dice.factcheck.ml.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.tokenizers.NGramTokenizer;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.*;

/**
 * @author DANISH AHMED on 7/11/2018
 */
public class ClassifierModelGenerator {
    private Instances trainInstance;
    private Instances testInstance;

    public ClassifierModelGenerator(String trainDataPath, String testDataPath) throws Exception {
        trainInstance = createInstance(trainDataPath);
        testInstance = createInstance(testDataPath);
    }

    public Instances getTrainInstance() {
        return this.trainInstance;
    }

    public Instances getTestInstance() {
        return this.testInstance;
    }

    private Instances createInstance(String dataFile) throws Exception {
        FileReader fileReader = new FileReader(dataFile);
        BufferedReader reader = new BufferedReader(fileReader);
        Instances instance = new Instances(reader);
        reader.close();

        StringToWordVector filterWordVector = applyStringFilter();
        filterWordVector.setInputFormat(instance);

        Instances filterInstance = Filter.useFilter(instance, filterWordVector);
        filterInstance.setClassIndex(filterInstance.numAttributes() - 1);
        instance.delete();

        return filterInstance;
    }

    public StringToWordVector applyStringFilter() {
        StringToWordVector filterWordVector = new StringToWordVector();

        filterWordVector.setWordsToKeep(1000000);
        filterWordVector.setTFTransform(true);
        filterWordVector.setLowerCaseTokens(true);
        filterWordVector.setOutputWordCounts(true);
        filterWordVector.setNormalizeDocLength(new SelectedTag(StringToWordVector.FILTER_NORMALIZE_ALL,StringToWordVector.TAGS_FILTER));

        NGramTokenizer t = new NGramTokenizer();
        t.setNGramMaxSize(3);
        t.setNGramMinSize(3);
        filterWordVector.setTokenizer(t);

        return filterWordVector;
    }

    public void writeClassifierAsModel(Classifier classifier, String ModelFileName) throws IOException {
        ObjectOutputStream oos;
        oos = new ObjectOutputStream(new FileOutputStream(ModelFileName));
        oos.writeObject(classifier);
        oos.flush();
        oos.close();
    }

    public static void main(String[] args) throws Exception {
        String trainPath = "data/eval/Train_Mix_Attr.arff";
        String testPath = "data/eval/Test_Mix2.arff";
        String modelWritePath = "data/classifierModels/";

        ClassifierModelGenerator classifierModelGenerator = new ClassifierModelGenerator(trainPath, testPath);
        Instances trainInstance = classifierModelGenerator.getTrainInstance();
        Instances testInstance = classifierModelGenerator.getTestInstance();

        Classifier randomForest = Classifiers.getRandomForest();
        ClassifierEvaluation classifierEvaluation = new ClassifierEvaluation(randomForest, trainInstance, testInstance);
        System.out.println("RandomForest:\t" + classifierEvaluation.getEvaluationAccuracy(classifierEvaluation.getEvaluation()));
        classifierModelGenerator.writeClassifierAsModel(randomForest, modelWritePath + "RandomForest.model");

        Classifier logistic = Classifiers.getLogistic();
        classifierEvaluation = new ClassifierEvaluation(logistic, trainInstance, testInstance);
        System.out.println("Logistic:\t" + classifierEvaluation.getEvaluationAccuracy(classifierEvaluation.getEvaluation()));
        classifierModelGenerator.writeClassifierAsModel(logistic, modelWritePath + "Logistic.model");

        Classifier j48 = Classifiers.getJ48();
        classifierEvaluation = new ClassifierEvaluation(j48, trainInstance, testInstance);
        System.out.println("J48:\t" + classifierEvaluation.getEvaluationAccuracy(classifierEvaluation.getEvaluation()));
        classifierModelGenerator.writeClassifierAsModel(j48, modelWritePath + "J48.model");

        Classifier rbfNetwork = Classifiers.getRBFNetwork();
        classifierEvaluation = new ClassifierEvaluation(rbfNetwork, trainInstance, testInstance);
        System.out.println("RBFNetwork:\t" + classifierEvaluation.getEvaluationAccuracy(classifierEvaluation.getEvaluation()));
        classifierModelGenerator.writeClassifierAsModel(rbfNetwork, modelWritePath + "RBFNetwork.model");

        /*
        * Only applying AdaBoost to those classifiers here whose scores were increased
        * */
        Classifier abJ48 = Classifiers.getAdaBoost(j48);
        classifierEvaluation = new ClassifierEvaluation(abJ48, trainInstance, testInstance);
        System.out.println("abJ48:\t" + classifierEvaluation.getEvaluationAccuracy(classifierEvaluation.getEvaluation()));
        classifierModelGenerator.writeClassifierAsModel(abJ48, modelWritePath + "AB_J48.model");

        Classifier abRawJ48 = Classifiers.getAdaBoost(new J48());
        classifierEvaluation = new ClassifierEvaluation(abRawJ48, trainInstance, testInstance);
        System.out.println("abRawJ48:\t" + classifierEvaluation.getEvaluationAccuracy(classifierEvaluation.getEvaluation()));
        classifierModelGenerator.writeClassifierAsModel(abRawJ48, modelWritePath + "AB_J48-Raw.model");

        Classifier abRBFNetwork = Classifiers.getAdaBoost(rbfNetwork);
        classifierEvaluation = new ClassifierEvaluation(abRBFNetwork, trainInstance, testInstance);
        System.out.println("abRBFNetwork:\t" + classifierEvaluation.getEvaluationAccuracy(classifierEvaluation.getEvaluation()));
        classifierModelGenerator.writeClassifierAsModel(abRBFNetwork, modelWritePath + "AB_RBFNetwork.model");

        /*
        * Let's combine models now
        * I have tried multiple combinations
        * here we will only pass and generate model for those classifiers whose combination performed best
        * */
        Classifier[] stackingClassifiers = {randomForest, logistic, rbfNetwork};
        Classifier stacking = Classifiers.getStacking(stackingClassifiers, j48);
        classifierEvaluation = new ClassifierEvaluation(stacking, trainInstance, testInstance);
        System.out.println("stacking:\t" + classifierEvaluation.getEvaluationAccuracy(classifierEvaluation.getEvaluation()));
        classifierModelGenerator.writeClassifierAsModel(stacking, modelWritePath + "Stacking.model");

        Classifier[] voteClassifiers = {randomForest, abRawJ48};
        Classifier vote = Classifiers.getVote(voteClassifiers);
        classifierEvaluation = new ClassifierEvaluation(vote, trainInstance, testInstance);
        System.out.println("vote:\t" + classifierEvaluation.getEvaluationAccuracy(classifierEvaluation.getEvaluation()));
        classifierModelGenerator.writeClassifierAsModel(vote, modelWritePath + "Vote.model");
    }
}
