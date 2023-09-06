package org.aksw.defacto.ml.feature.evidence;

import org.aksw.defacto.evidence.Evidence;

import weka.classifiers.Classifier;

public interface Scorer {

	public Classifier loadClassifier();
    public Classifier loadClassifier(String predicate);
	
    public Double scoreEvidence(Evidence evidence);
}
