package org.dice.factcheck.ml.feature.fact.impl;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;

import java.util.List;
import java.util.Properties;

/**
 * @author DANISH AHMED on 6/30/2018
 */
public class ContextProofNegationIdentifierFeature implements FactFeature {

    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
        BoaPatternSearcher searcher = new BoaPatternSearcher();
        float score = (float) 0.0;
        String context = proof.getProofPhrase().toLowerCase();
        String predicate = "";
        List<Pattern> patterns = searcher.querySolrIndex(evidence.getModel().getPropertyUri(), 20, 0, proof.getLanguage());

        for ( Pattern p : patterns ) {
            if ( p.getNormalized().trim().isEmpty() ) continue;
            if ( proof.getProofPhrase().toLowerCase().contains(p.getNormalized()) )
            {
                predicate = p.getNormalized().trim();
                break;
            }
        }

        String[] properties = {"annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref"};
        List<CoreMap> sentences = getMappedSentences(properties, context);
        score = processNegation(sentences, predicate);

        proof.getFeatures().setValue(AbstractFactFeatures.CONTEXT_IN_FAVOR_PREDICATE, score);
    }

    public StanfordCoreNLP getPipeline(String propertyKey, String propertyValue) {
        Properties props = new Properties();
        props.setProperty(propertyKey, propertyValue);
        return new StanfordCoreNLP(props);
    }

    public Annotation annotateDocument(String context, StanfordCoreNLP pipeline) {
        Annotation document = new Annotation(context);
        pipeline.annotate(document);

        return document;
    }

    public List<CoreMap> getMappedSentences(String[] properties, String context) {
        String propertyKey = properties[0];
        String propertyValue = properties[1];

        StanfordCoreNLP pipeline = getPipeline(propertyKey, propertyValue);
        Annotation document = annotateDocument(context, pipeline);

        return document.get(CoreAnnotations.SentencesAnnotation.class);
    }

    public float processNegation(List<CoreMap> sentences, String predicate) {
        Boolean hasNegation = false;
        Boolean hasPredicateNegation = false;

        for(CoreMap sentence: sentences) {
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                if (pos.equals("RB")) {
                    hasNegation = true;
                    break;
                }
            }

            if (hasNegation) {
                Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
                SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

                //TODO: Call function to determine if negation is of input predicate or other verrb
                hasPredicateNegation = negationOfInputPredicate();
                if (hasPredicateNegation)
                    break;
            }
        }
        return 0;
    }

    public Boolean negationOfInputPredicate() {
        return false;
    }
}
