/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.BlockDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.DiceSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.EuclideanDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Jaro;
import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;
import uk.ac.shef.wit.simmetrics.similaritymetrics.OverlapCoefficient;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

/**
 * @author Zafar Syed <zsyed@mail.uni-paderborn.de>
 *
 */
public class PattyPatterFeature implements FactFeature {
	
	SmithWaterman smithWaterman = new SmithWaterman();
    QGramsDistance qgrams		= new QGramsDistance();
    Levenshtein lev				= new Levenshtein();
    BoaPatternSearcher searcher = new BoaPatternSearcher();

    @Override
    public void extractFeature(ComplexProof proof, Evidence evidence) {
    	
    	HashMap<String, Float> patterns = new LinkedHashMap<>();
    	try {
			BufferedReader buf = new BufferedReader(new FileReader(""));
			String lineJustFetched = null;
			String[] wordsArray;
			
        	float score = (float) 0.0;
        	while(true){
                lineJustFetched = buf.readLine();
                if(lineJustFetched == null){  
                    break; 
                }else{
                	wordsArray = lineJustFetched.split("\t");
                	patterns.put(wordsArray[0], Float.parseFloat(wordsArray[1]));
                }
                }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if ( proof.getProofPhrase().trim().isEmpty() ) return; 
        
        float smithWatermanSimilarity = 0f;
        float qgramsSimilarity = 0f;
        float levSimilarity = 0f;
        float leveboa = 0f;
        float qgramboa = 0f;
        float smithboa = 0f;
        int patternCounter = 0, patternNormalizedCounter = 0;
        Iterator it = patterns.entrySet().iterator();
        boolean leve=false,smith=false,qgram = false;
        
        while(it.hasNext())
        {
        	Map.Entry<String, Float> pair = (Map.Entry)it.next();
        	if ( pair.getValue().toString().trim().isEmpty() ) continue;
//        	if ( !proof.getProofPhrase().contains(p.normalize()) ) continue;
        	float swSim = smithWaterman.getSimilarity(pair.getKey().toString(), proof.getProofPhrase());
			if ( swSim > smithWatermanSimilarity ) {
				
				smithWatermanSimilarity = swSim;
				smith=true;
				smithboa = pair.getValue();
			}
			
			float qgramsSim = qgrams.getSimilarity(pair.getKey().toString(), proof.getProofPhrase());
			if ( qgramsSim > qgramsSimilarity ) {
				
				qgramsSimilarity = qgramsSim; 
				qgram = true;
				qgramboa = pair.getValue();
			}
			
			float levSim = lev.getSimilarity(pair.getKey().toString(), proof.getProofPhrase());
			if ( levSim > levSimilarity ) {
				
				levSimilarity = levSim; 
				leve = true;
				leveboa = pair.getValue();
			}
			//System.out.println(proof.getProofPhrase()+"==== "+p.getNormalized());
			if ( proof.getProofPhrase().contains(pair.getKey().toString()) ) patternCounter++; 
			//System.out.println(proof.getNormalizedProofPhrase()+" === "+p.getNormalized());
			if ( proof.getNormalizedProofPhrase().toLowerCase().contains(pair.getKey().toString()) ) patternNormalizedCounter++; 
        }
        
        System.out.println(proof.getProofPhrase());
        System.out.println("BOA Pattern Count "+patternCounter);
        //System.out.println("BOA_PATTERN_COUNT for is "+patternCounter);
        System.out.println("BOA pattern normalized count "+patternNormalizedCounter);
        //System.out.println("BOA_PATTERN_NORMALIZED_COUNT for is "+patternNormalizedCounter);
        
        if ( leve ) {
        	
        	System.out.println("Lev similarity "+levSimilarity);
        	//System.out.println("LEVENSHTEIN for is "+levSimilarity);
            System.out.println("Leven boa score "+leveboa);
            //System.out.println("LEVENSHTEIN_BOA_SCORE for is "+levPattern.boaScore);
        }
        	
    	if ( qgram ) {
    		
    		System.out.println("qgram similarity "+qgramsSimilarity);
    		//System.out.println("QGRAMS for is "+qgramsSimilarity);
            System.out.println("Qgram boa score "+qgramboa);
            //System.out.println("QGRAMS_BOA_SCORE for is "+qgramPattern.boaScore);
    	}
    		
    	if ( smith) {
    		
    		System.out.println("Smith similarity "+smithWatermanSimilarity);
    		//System.out.println("SMITH_WATERMAN for is "+smithWatermanSimilarity);
        	System.out.println("Smith boa score "+smithboa);
        	//System.out.println("SMITH_WATERMAN for is "+swPattern.boaScore);
    	}
    }
    
    
    public static void main(String[] args) {
		
    	String longTest = "oubleCli . . . Aprimo 05/09/05 Selectica Acquires Determine Software Products - Selectica announced the acquisition of the contract managem".toLowerCase();
//    	String pattern = "was acquires by";
    	String pattern = "acquires";
    	
    	List<? extends AbstractStringMetric> metrics = Arrays.asList(
//    			new Levenshtein(),  new BlockDistance(), new OverlapCoefficient(), new DiceSimilarity(),
//    			new JaccardSimilarity(), new EuclideanDistance(), new Jaro(), new JaroWinkler(), 
    			new SmithWaterman(),
    			new Levenshtein(),
    			new QGramsDistance());
    			//, new NeedlemanWunch());
    	
    	for ( AbstractStringMetric metric : metrics) {
    		
    		System.out.println(metric.getShortDescriptionString() + ":\t" + metric.getSimilarity("in 1922 he was obel awarded the 1921 Nobel Prize in Physics \"for his services to Theoretical Physics, and especially for his discovery of the law of the photoelectric effect\".", "was awarded"));
    	}
	}
}
