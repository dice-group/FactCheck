package org.dice.factcheck.ml.feature.fact.impl;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
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
    public String predicate = "";
    public String subject = "";
    public String object = "";
    public Evidence evidence;

    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
        BoaPatternSearcher searcher = new BoaPatternSearcher();
        float score = (float) 0.0;

        this.evidence = evidence;
        subject = proof.getSubject().toLowerCase();
        object = proof.getObject().toLowerCase();
//        String context = proof.getProofPhrase().toLowerCase();
        String context = "albert einstein is not a winner of nobel prize in physics .";
        List<Pattern> patterns = searcher.querySolrIndex(evidence.getModel().getPropertyUri(), 20, 0, proof.getLanguage());

        System.out.println(context);

        for ( Pattern p : patterns ) {
            if ( p.getNormalized().trim().isEmpty() ) continue;
            if ( proof.getProofPhrase().toLowerCase().contains(p.getNormalized()) ) {
                predicate = p.getNormalized().trim();
                break;
            }
        }

        predicate = "winner";

        String[] properties = {"annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref"};
        List<CoreMap> sentences = getMappedSentences(properties, context);
//        score = 1 - processNegation(sentences);
        double dependencyValue = proof.getFeatures().value(AbstractFactFeatures.DEPENDENCY_SUBJECT_OBJECT);
        score = (float)(dependencyValue) - processNegation(sentences);

        proof.getFeatures().setValue(AbstractFactFeatures.DEPENDENCY_SUBJECT_OBJECT, score);
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
        /*String propertyKey = properties[0];
        String propertyValue = properties[1];

        StanfordCoreNLP pipeline = getPipeline(propertyKey, propertyValue);
        Annotation document = annotateDocument(context, pipeline);

        return document.get(CoreAnnotations.SentencesAnnotation.class);*/
        Annotation document = evidence.getModel().corenlpClient.corefAnnotation(context);
        return document.get(CoreAnnotations.SentencesAnnotation.class);
    }

    public float processNegation(List<CoreMap> sentences) {
        Boolean hasNegation = false;
        for(CoreMap sentence: sentences) {
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                if (pos.equals("RB")) {
                    hasNegation = true;
                    break;
                }
            }

            if (hasNegation) {
                SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
                return negationOfInputPredicate(dependencies);
            }
        }
        return 0;
    }

    public float negationOfInputPredicate(SemanticGraph dependencies) {
        Boolean sentenceNegation = false;
        float score = 0;
        for (IndexedWord rootWord : dependencies.getRoots()) {
            String sentenceSubject = "";
            String sentenceObject = "";
            StringBuilder objectAppend = new StringBuilder();
            if (!rootWord.originalText().equals(predicate))
                continue;
            for (SemanticGraphEdge predEdge : dependencies.getOutEdgesSorted(rootWord)) {
                if (predEdge.getRelation().getShortName().contains("nsubj")) {
                    sentenceSubject = getCompoundSubject(predEdge.getTarget(), dependencies);
                } else if (predEdge.getRelation().getShortName().contains("dobj")) {
                    sentenceObject = getCompoundObject(predEdge.getTarget(), dependencies);
                } else if (predEdge.getRelation().getShortName().contains("nmod")) {
                    objectAppend.append(predEdge.getTarget().originalText().toLowerCase()).append(" ");
                }else if (predEdge.getRelation().getShortName().equals("neg")) {
                    sentenceNegation = true;
                }
            }
            if (sentenceNegation) {
                sentenceObject = sentenceObject.trim() + " " + objectAppend.toString().trim();
                float subNegation = actorMatchesNegation(sentenceSubject, subject);
                float objNegation = actorMatchesNegation(sentenceObject, object);
                score = (subNegation + objNegation) / 2;
                return  score;
            }
        }
        return score;
    }

    public String getCompoundSubject(IndexedWord subj, SemanticGraph dependencies){
        StringBuilder subject = new StringBuilder(subj.originalText().toLowerCase() + " ");
        for (SemanticGraphEdge predEdge : dependencies.getOutEdgesSorted(subj)) {
            subject.append(predEdge.getTarget().originalText().toLowerCase()).append(" ");
        }
        return String.valueOf(subject);
    }

    public String getCompoundObject(IndexedWord obj, SemanticGraph dependencies){
        StringBuilder object = new StringBuilder(obj.originalText().toLowerCase() + " ");
        for (SemanticGraphEdge predEdge : dependencies.getOutEdgesSorted(obj)) {
            object.append(predEdge.getTarget().originalText().toLowerCase()).append(" ");
        }
        return String.valueOf(object);
    }

    public float actorMatchesNegation(String comparator, String compareWith) {

        float subjPartsCount = compareWith.split(" ").length;
        if (subjPartsCount == 0)
            subjPartsCount = 1;

        String[] comp = comparator.split(" ");
        if (comp.length == 0)
            if (compareWith.contains(comparator))
                return 1;

        float matchCount = 0;
        for (String subjPart : comp)
            if (compareWith.contains(subjPart))
                matchCount++;

        return (matchCount / subjPartsCount);
    }

    public static void main(String[] args) {
        String predicate = "winner";
        String subject = "albert einstein";
        String object = "noble prize in physics";
        String context = "albert einstein is not a winner of nobel prize in physics .";
        ContextProofNegationIdentifierFeature negationIdentifier = new ContextProofNegationIdentifierFeature();
        negationIdentifier.predicate = predicate;
        negationIdentifier.subject = subject;
        negationIdentifier.object = object;

        String[] properties = {"annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref"};
        List<CoreMap> sentences = negationIdentifier.getMappedSentences(properties, context);
        System.out.println(negationIdentifier.processNegation(sentences));
    }
}
