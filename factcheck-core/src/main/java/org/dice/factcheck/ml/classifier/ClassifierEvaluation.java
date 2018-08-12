package org.dice.factcheck.ml.classifier;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;

/**
 * @author DANISH AHMED on 7/11/2018
 */
public class ClassifierEvaluation {
    private Classifier classifier;
    private Instances trainInstance;
    private Instances testInstance;

    private Evaluation evaluation;

    public ClassifierEvaluation(Classifier classifier, Instances trainInstance, Instances testInstance) throws Exception {
        this.classifier = classifier;
        this.trainInstance = trainInstance;
        this.testInstance = testInstance;

        this.evaluation = evaluateClassification();
    }

    private Evaluation evaluateClassification() throws Exception {
        classifier.buildClassifier(trainInstance);

        Evaluation evaluation = new Evaluation(trainInstance);
        evaluation.evaluateModel(classifier, testInstance);

        return evaluation;
    }

    public double getEvaluationAccuracy(Evaluation evaluation) {
        return evaluation.pctCorrect();
    }

    public Evaluation getEvaluation() {
        return this.evaluation;
    }
}
