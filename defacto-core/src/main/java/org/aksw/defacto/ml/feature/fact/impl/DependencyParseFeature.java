/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.FactFeature;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils; 

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation; 
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation; 
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
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
/**
 * @author Zafar Syed <zsyed@mail.uni-paderborn.de>
 *
 */
public class DependencyParseFeature implements FactFeature {

	private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
	private StanfordCoreNLPClient pipeline;
	public static LexicalizedParser parser;
	static boolean found = false;

	public DependencyParseFeature() {

		this.parser = LexicalizedParser.loadModel(PCG_MODEL);
	}

	@Override
	public void extractFeature(ComplexProof proof, Evidence evidence) {

		this.pipeline = proof.getModel().pipeline1;
		List<TypedDependency> tdl = null;
		Annotation doc = new Annotation(proof.getProofPhrase());
		pipeline.annotate(doc);
		for(CoreMap sentence: doc.get(SentencesAnnotation.class)) {
			List<CoreLabel> tokens = sentence.get(TokensAnnotation.class);
			Tree tree = parser.parse(tokens);
			TreebankLanguagePack tlp = new PennTreebankLanguagePack();
			GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
			GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
			tdl = gs.typedDependenciesCCprocessed();
			//System.out.println(tdl.toString());
			int size = tdl.size(); 
			for(int i = 0; i < size; i++){ 
				TypedDependency td = tdl.get(i); 

				{ 
					//TreeGraphNode n = new TreeGraphNode(td.dep()); 
					if(td.gov().ner() == null && !td.reln().toString().contains("root")){ 
						td.gov().setNER(tokens.get(td.gov().index()-1).ner()); 
						td.gov().setBeginPosition(tokens.get(td.gov().index()-1).beginPosition()); 
						td.gov().setEndPosition(tokens.get(td.gov().index()-1).endPosition()); 
						td.gov().setLemma(tokens.get(td.gov().index()-1).lemma());
						
					} 
				} 
				{ 
					//TreeGraphNode n = new TreeGraphNode(td.gov()); 
					if(td.dep().ner() == null){ 
						td.dep().setNER(tokens.get(td.dep().index()-1).ner()); 
						td.dep().setBeginPosition(tokens.get(td.dep().index()-1).beginPosition()); 
						td.dep().setEndPosition(tokens.get(td.dep().index()-1).endPosition()); 
						td.dep().setLemma(tokens.get(td.dep().index()-1).lemma()); 
					} 
				} 
			}
			
			String[] subLabels = proof.getSubject().split(" ");
			String[] objLabels = proof.getObject().split(" ");
			Iterator<TypedDependency> it = tdl.iterator();
			while(it.hasNext())
			{
				TypedDependency td = it.next();
				//System.out.println(td.toString());
				if(td.gov().toString().contains(proof.getWebSite().getPredicate()) && (td.dep().toString().contains(subLabels[subLabels.length-1]) || td.dep().tag().toString().equals("PRP")))
					System.out.println(td.reln()+"("+td.gov().lemma()+","+td.dep().lemma()+")");
				if(td.gov().toString().contains(proof.getWebSite().getPredicate()) && td.dep().toString().contains(objLabels[objLabels.length-1]))
					System.out.println(td.reln()+"("+td.gov().lemma()+","+td.dep().lemma()+")");
				if(td.gov().toString().contains(subLabels[subLabels.length-1]) && td.dep().toString().contains(objLabels[objLabels.length-1]))
					System.out.println(td.reln()+"("+td.gov().lemma()+","+td.dep().lemma()+")");
			}

		}

	}   

	public static void main(String[] args) {

	}
}
