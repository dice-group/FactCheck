package org.dice.factcheck.ml.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.functions.Logistic;
import weka.classifiers.functions.RBFNetwork;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Stacking;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

/**
 * @author DANISH AHMED on 7/11/2018
 */
public class Classifiers {

    public static Classifier getRandomForest(){
        Classifier classifier = new RandomForest();
        ((RandomForest) classifier).setNumTrees(25);
        ((RandomForest) classifier).setSeed(2);
        return classifier;
    }

    public static Classifier getLogistic(){
        Classifier classifier = new Logistic();
        ((Logistic) classifier).setMaxIts(37);
        return classifier;
    }

    public static Classifier getJ48(){
        Classifier classifier = new J48();
        ((J48) classifier).setUnpruned(true);
        return classifier;
    }

    public static Classifier getRBFNetwork(){
        Classifier classifier = new RBFNetwork();
        ((RBFNetwork) classifier).setNumClusters(100);
        ((RBFNetwork) classifier).setClusteringSeed(6);
        return classifier;
    }

    public static Classifier getAdaBoost(Classifier toBoostClassifier) {
        Classifier classifier = new AdaBoostM1();
        ((AdaBoostM1) classifier).setClassifier(toBoostClassifier);
        return classifier;
    }

    public static Classifier getStacking(Classifier[] classifiers, Classifier metaClassifier) {
        Classifier classifier = new Stacking();
        ((Stacking) classifier).setMetaClassifier(metaClassifier);
        ((Stacking) classifier).setClassifiers(classifiers);
        return classifier;
    }

    public static Classifier getVote(Classifier[] classifiers) {
        Classifier classifier = new Vote();
        ((Vote) classifier).setClassifiers(classifiers);
        return classifier;
    }
}
