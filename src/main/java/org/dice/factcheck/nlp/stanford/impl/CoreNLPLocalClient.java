package org.dice.factcheck.nlp.stanford.impl;

import java.util.Properties;

import org.dice.factcheck.nlp.stanford.CoreNLPClient;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

public class CoreNLPLocalClient implements CoreNLPClient {
	
    private StanfordCoreNLP pipelineSentence;
    private StanfordCoreNLP pipelineCoref;
    
    public CoreNLPLocalClient() {
		
    	Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");
	    this.pipelineSentence = new StanfordCoreNLP(props);
        Properties props1 = new Properties();
        props1.put("tokenize.language", "English");
	    props1.put("pos.model", "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
	    props1.put("ner.model","edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
	    props1.put("ner.applyNumericClassifiers", "false");
	    props1.put("ner.useSUTime", "false");
	    props1.put("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
	    props1.put("coref.algorithm", "statistical");
	    props1.put("coref.model", "edu/stanford/nlp/models/coref/statistical/ranking_model.ser.gz");
	    props1.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, mention, coref");
	    this.pipelineCoref = new StanfordCoreNLP(props1);
	}

	@Override
	public Annotation sentenceAnnotation(String document) {
		
		Annotation annotatedDoc = new Annotation(document);
		this.pipelineSentence.annotate(annotatedDoc);
		return annotatedDoc;
	}

	@Override
	public Annotation corefAnnotation(String document) {
		
		Annotation annotatedDoc = new Annotation(document);
		this.pipelineCoref.annotate(annotatedDoc);
		return annotatedDoc;
	}

}
