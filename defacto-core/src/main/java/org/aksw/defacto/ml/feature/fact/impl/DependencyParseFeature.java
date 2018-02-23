/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils; 

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation; 
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation; 
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.StanfordCoreNLPClient;
import edu.stanford.nlp.trees.EnglishGrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency; 
import edu.stanford.nlp.semgraph.SemanticGraph; 
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.util.CoreMap;
import weka.core.Attribute;
/**
 * @author Zafar Syed <zsyed@mail.uni-paderborn.de>
 *
 */
public class DependencyParseFeature implements FactFeature {

	private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private StanfordCoreNLPClient pipeline;
	public static LexicalizedParser parser;
	static boolean found = false;
	BoaPatternSearcher searcher = new BoaPatternSearcher();

	public DependencyParseFeature() {

		this.parser = LexicalizedParser.loadModel(PCG_MODEL);
	}

	@Override
	public void extractFeature(ComplexProof proof, Evidence evidence) {

		List<Pattern> patterns = searcher.querySolrIndex(evidence.getModel().getPropertyUri(), 20, 0, proof.getLanguage());
		float score = (float) 0.0;
		String patternString = "";
		for ( Pattern p : patterns ) {
			if ( p.getNormalized().trim().isEmpty() ) continue;
			if ( proof.getProofPhrase().toLowerCase().contains(p.getNormalized()) )
			{
				patternString = p.getNormalized().trim();
				break;
			}
		}

		if(!(patternString==""))
		{
		this.pipeline = proof.getModel().pipeline;
		List<TypedDependency> tdl = null;
		Annotation doc = new Annotation(proof.getProofPhrase());
		pipeline.annotate(doc);
		for(CoreMap sentence: doc.get(SentencesAnnotation.class)) {
			if(sentence.get(CoreAnnotations.TextAnnotation.class).toLowerCase().contains(patternString) && sentence.get(CoreAnnotations.TextAnnotation.class).split(" ").length<80)
			{
			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
			Tree tree = parser.parse(tokens);
			TreebankLanguagePack tlp = new PennTreebankLanguagePack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
			tdl = gs.typedDependenciesCCprocessed();

			List<String> subLabels = new ArrayList<String>();
			List<String> objLabels = new ArrayList<String>();
			
			for (String str : proof.getSubject().split(" ")) {
				subLabels.add(str.toLowerCase());
			}
			
			for (String str : proof.getObject().split(" ")) {
				objLabels.add(str.toLowerCase());
			}

			Iterator<TypedDependency> it = tdl.iterator();
			while(it.hasNext())
			{
				TypedDependency td = it.next();
				if((td.gov().toString().toLowerCase().contains(patternString) && (subLabels.contains(td.dep().originalText().toLowerCase())))
						|| (td.dep().toString().toLowerCase().contains(patternString) && (subLabels.contains(td.gov().originalText().toLowerCase()))))
					score = (float) (score + 0.5);
				if((subLabels.contains(td.dep().originalText()) && (objLabels.contains(td.gov().originalText().toLowerCase())))
						|| (subLabels.contains(td.dep().originalText()) && (objLabels.contains(td.gov().originalText().toLowerCase()))))
						score = (float) (score + 0.25);
				if((td.gov().toString().toLowerCase().contains(patternString) && (objLabels.contains(td.dep().originalText().toLowerCase())))
						|| (td.dep().toString().toLowerCase().contains(patternString) && (objLabels.contains(td.gov().originalText().toLowerCase()))))
					score = (float) (score + 0.25);
				
			}
			if(sentence.get(CoreAnnotations.TextAnnotation.class).toLowerCase().contains(proof.getSubject().toLowerCase()))
				score = (float) (score + 0.25);			
			}
		}
		}

		if(score > (float)1.0)
			score = (float) 1.0;
		proof.getFeatures().setValue(AbstractFactFeatures.DEPENDENCY_SUBJECT_OBJECT, score);

	}   

	public static void main(String[] args) {

	}
}
