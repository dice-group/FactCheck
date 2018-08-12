package org.dice.factcheck.ml.feature.fact.impl;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation; 
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency; 
import edu.stanford.nlp.util.CoreMap; 
/**
 * @author Zafar Syed <zsyed@mail.uni-paderborn.de>
 *
 */
public class DependencyParseFeature implements FactFeature {

	private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	public LexicalizedParser parser;
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
			List<TypedDependency> tdl = null;
			Annotation doc = evidence.getModel().corenlpClient.sentenceAnnotation(proof.getProofPhrase());
			for(CoreMap sentence: doc.get(SentencesAnnotation.class)) {
				if(sentence.get(CoreAnnotations.TextAnnotation.class).toLowerCase().contains(patternString) && sentence.get(CoreAnnotations.TextAnnotation.class).split(" ").length<30)
				{
					List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
					Tree tree = parser.parse(tokens);
					TreebankLanguagePack tlp = new PennTreebankLanguagePack();
					GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
					GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
					tdl = gs.typedDependenciesEnhanced();


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
							score = (float) (score + 0.5);
						if((td.gov().toString().toLowerCase().contains(patternString) && (objLabels.contains(td.dep().originalText().toLowerCase())))
								|| (td.dep().toString().toLowerCase().contains(patternString) && (objLabels.contains(td.gov().originalText().toLowerCase()))))
							score = (float) (score + 0.5);
					}							
				}
			}
		}
		proof.getFeatures().setValue(AbstractFactFeatures.DEPENDENCY_SUBJECT_OBJECT, score);

	}   

	public static void main(String[] args) {

	}
}

