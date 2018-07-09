package org.dice.factcheck.nlp.stanford;

import edu.stanford.nlp.pipeline.Annotation;

public interface CoreNLPClient {

	public Annotation sentenceAnnotation(String document);
	
	public Annotation corefAnnotation(String document);

	public Annotation negationAnnotation(String document);
}
