package org.dice.factcheck.nlp.stanford.impl;

import java.util.Properties;

import org.aksw.defacto.Defacto;
import org.dice.factcheck.nlp.stanford.CoreNLPClient;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;

public class CoreNLPServerClient implements CoreNLPClient {

	// We need two pipelines, one for Sentence splitting entire document
	// Other for applying Coreference on extracted sentences
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

		Properties propSentences = new Properties();
		propSentences.put("annotators", "tokenize, ssplit");
		this.pipelineSentence = new StanfordCoreNLPClient(propSentences, "http://"+CORENLP_SERVER1, Integer.parseInt(CORENLP_PORT1), 8);
		Properties propCoreference = new Properties();
		propCoreference.put("tokenize.language", "English");
		propCoreference.put("pos.model", "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");
		propCoreference.put("ner.model","edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");
		propCoreference.put("ner.applyNumericClassifiers", "false");
		propCoreference.put("ner.useSUTime", "false");
		propCoreference.put("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");
		propCoreference.put("coref.algorithm", "statistical");
		propCoreference.put("coref.model", "edu/stanford/nlp/models/coref/statistical/ranking_model.ser.gz");
		propCoreference.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, mention, coref");
		this.pipelineCoref = new StanfordCoreNLPClient(propCoreference, "http://"+CORENLP_SERVER2, Integer.parseInt(CORENLP_PORT2), 8);
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
