/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import java.util.Arrays;
import java.util.List;

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
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class BoaFeature implements FactFeature {

	SmithWaterman smithWaterman = new SmithWaterman();
	QGramsDistance qgrams		= new QGramsDistance();
	Levenshtein lev				= new Levenshtein();
	BoaPatternSearcher searcher = new BoaPatternSearcher();

	@Override
	public void extractFeature(ComplexProof proof, Evidence evidence) {

		// we set this to 0 and over write it if we find a pattern
		proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN_BOA_SCORE, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN, 0);

		proof.getFeatures().setValue(AbstractFactFeatures.QGRAMS_BOA_SCORE, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.QGRAMS, 0);

		proof.getFeatures().setValue(AbstractFactFeatures.LEVENSHTEIN_BOA_SCORE, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.LEVENSHTEIN, 0);

		proof.getFeatures().setValue(AbstractFactFeatures.BOA_PATTERN_COUNT, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.BOA_PATTERN_NORMALIZED_COUNT, 0);

		if ( proof.getProofPhrase().trim().isEmpty() ) return; 

		List<Pattern> patterns = searcher.querySolrIndex(evidence.getModel().getPropertyUri(), 20, 0, proof.getLanguage());

		float smithWatermanSimilarity = 0f;
		float qgramsSimilarity = 0f;
		float levSimilarity = 0f;
		int patternCounter = 0, patternNormalizedCounter = 0;
		Pattern swPattern = null;
		Pattern qgramPattern = null;
		Pattern levPattern = null;
		String patternString = "";
		boolean subjectObject = false;


		for ( Pattern p : patterns ) {

			if ( p.getNormalized().trim().isEmpty() ) continue;

			float swSimilarity = smithWaterman.getSimilarity(p.naturalLanguageRepresentationWithoutVariables, proof.getNormalizedProofPhrase());
			if ( swSimilarity > smithWatermanSimilarity ) {

				smithWatermanSimilarity = swSimilarity;
				swPattern = p;
			}

			float qgramsSimil = qgrams.getSimilarity(p.naturalLanguageRepresentationWithoutVariables, proof.getNormalizedProofPhrase());
			if ( qgramsSimil > qgramsSimilarity ) {

				qgramsSimilarity = qgramsSimil; 
				qgramPattern = p;
			}

			float levSimil = lev.getSimilarity(p.naturalLanguageRepresentationWithoutVariables, proof.getNormalizedProofPhrase());
			if ( levSimil > levSimilarity ) {

				levSimilarity = levSimil; 
				levPattern = p;
			}
			
			if ( proof.getProofPhrase().contains(p.getNormalized()) ) patternCounter++; 
			if ( proof.getNormalizedProofPhrase().toLowerCase().contains(p.getNormalized()) ) patternNormalizedCounter++;

			if(!(org.apache.commons.lang3.StringUtils.substringBetween(proof.getNormalizedProofPhrase().toLowerCase(), proof.getSubject().toLowerCase(), proof.getObject().toLowerCase())==null))
			{
				patternString = org.apache.commons.lang3.StringUtils.substringBetween(proof.getNormalizedProofPhrase().toLowerCase(), proof.getSubject().toLowerCase(), proof.getObject().toLowerCase());
				subjectObject = true;
			}

			else
			{
				patternString = org.apache.commons.lang3.StringUtils.substringBetween(proof.getNormalizedProofPhrase().toLowerCase(), proof.getObject().toLowerCase(), proof.getSubject().toLowerCase());
				subjectObject = false;
			}

			while(patternString!=null)
			{

				float swSim = smithWaterman.getSimilarity(p.naturalLanguageRepresentationWithoutVariables, patternString.trim());
				if ( swSim > smithWatermanSimilarity ) {

					smithWatermanSimilarity = swSim;
					swPattern = p;
				}

				float qgramsSim = qgrams.getSimilarity(p.naturalLanguageRepresentationWithoutVariables, patternString.trim());
				if ( qgramsSim > qgramsSimilarity ) {

					qgramsSimilarity = qgramsSim; 
					qgramPattern = p;
				}

				float levSim = lev.getSimilarity(p.naturalLanguageRepresentationWithoutVariables, patternString.trim());
				if ( levSim > levSimilarity ) {

					levSimilarity = levSim; 
					levPattern = p;
				}			
				if(subjectObject)
					patternString = org.apache.commons.lang3.StringUtils.substringBetween(patternString+proof.getObject().toLowerCase(), proof.getSubject().toLowerCase(), proof.getObject().toLowerCase());
				else
					patternString = org.apache.commons.lang3.StringUtils.substringBetween(patternString+proof.getSubject().toLowerCase(), proof.getObject().toLowerCase(), proof.getSubject().toLowerCase());

			}


		}

		proof.getFeatures().setValue(AbstractFactFeatures.BOA_PATTERN_COUNT, patternCounter);
		proof.getFeatures().setValue(AbstractFactFeatures.BOA_PATTERN_NORMALIZED_COUNT, patternNormalizedCounter);

		if ( levPattern != null ) {

			proof.getFeatures().setValue(AbstractFactFeatures.LEVENSHTEIN, levSimilarity);
			proof.getFeatures().setValue(AbstractFactFeatures.LEVENSHTEIN_BOA_SCORE, levPattern.boaScore);
		}

		if ( qgramPattern != null ) {

			proof.getFeatures().setValue(AbstractFactFeatures.QGRAMS, qgramsSimilarity);
			proof.getFeatures().setValue(AbstractFactFeatures.QGRAMS_BOA_SCORE, qgramPattern.boaScore);
		}

		if ( swPattern != null ) {

			proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN, smithWatermanSimilarity);
			proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN_BOA_SCORE, swPattern.boaScore);
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

		for ( AbstractStringMetric metric : metrics) {

			System.out.println(metric.getShortDescriptionString() + ":\t" + metric.getSimilarity("a b c d", "b c d e"));
		}
	}
}
