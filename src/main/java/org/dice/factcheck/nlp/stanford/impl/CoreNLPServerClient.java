package org.dice.factcheck.nlp.stanford.impl;

import java.util.Properties;

import org.aksw.defacto.Defacto;
import org.dice.factcheck.nlp.stanford.CoreNLPClient;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;

public class CoreNLPServerClient implements CoreNLPClient {
	
	private StanfordCoreNLPClient pipelineSentence;
    private StanfordCoreNLPClient pipelineCoref;
    private String CORENLP_SERVER1;
    private String CORENLP_PORT1;
    private String CORENLP_SERVER2;
    private String CORENLP_PORT2;

	public CoreNLPServerClient() {
		
		this.CORENLP_SERVER1 = Defacto.DEFACTO_CONFIG.getStringSetting("corenlp", "SERVER_ADDRESS1");
        this.CORENLP_SERVER2 = Defacto.DEFACTO_CONFIG.getStringSetting("corenlp", "SERVER_ADDRESS2");
        this.CORENLP_PORT1 = Defacto.DEFACTO_CONFIG.getStringSetting("corenlp", "PORT_NUMBER1");
        this.CORENLP_PORT2 = Defacto.DEFACTO_CONFIG.getStringSetting("corenlp", "PORT_NUMBER2");
        
        Properties props = new Properties();
	    props.put("annotators", "tokenize, ssplit");
	    this.pipelineSentence = new StanfordCoreNLPClient(props, "http://"+CORENLP_SERVER1, Integer.parseInt(CORENLP_PORT1), 8);
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
	    this.pipelineCoref = new StanfordCoreNLPClient(props1, "http://"+CORENLP_SERVER2, Integer.parseInt(CORENLP_PORT2), 8);
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
