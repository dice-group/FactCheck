package org.dice.factcheck.ml.feature.fact.impl;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.dice.factcheck.search.engine.elastic.ElasticSearchEngine;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.QGramsDistance;
import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

/**
 * @author Zafar Syed <zsyed@mail.uni-paderborn.de>
 *
 */
public class PattyPatternFeature implements FactFeature {

	private static SmithWaterman smithWaterman = new SmithWaterman();
	private static QGramsDistance qgrams		= new QGramsDistance();
	private static Levenshtein lev				= new Levenshtein();
	private static BoaPatternSearcher searcher = new BoaPatternSearcher();
	private static float smithWatermanSimilarity = 0f;
	private static float qgramsSimilarity = 0f;
	private static float levSimilarity = 0f;
	private static double smithWatermanScore = 0.00;
	private static double qgramsSimilarityScore = 0.00;
	private static double levSimilarityScore = 0.00;
	private static int patternCounter = 0, patternNormalizedCounter = 0;
	private static String proofSubString = "";
	private static boolean subjectObject = false;
	private Response response;
	private String json;
	private JsonNode rootNode;
	private ObjectMapper mapper = new ObjectMapper();
	private static Logger logger =  Logger.getLogger(PattyPatternFeature.class);

	@Override
	public void extractFeature(ComplexProof proof, Evidence evidence) {

		RestClient restClientobj = RestClient.builder(new HttpHost("131.234.28.255" , 6060, "http")).build();
		//Set all distances to zero
		proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN_BOA_SCORE, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN, 0);

		proof.getFeatures().setValue(AbstractFactFeatures.QGRAMS_BOA_SCORE, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.QGRAMS, 0);

		proof.getFeatures().setValue(AbstractFactFeatures.LEVENSHTEIN_BOA_SCORE, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.LEVENSHTEIN, 0);

		proof.getFeatures().setValue(AbstractFactFeatures.BOA_PATTERN_COUNT, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.BOA_PATTERN_NORMALIZED_COUNT, 0);

		if ( proof.getProofPhrase().trim().isEmpty() ) return; 

		// Get all the patterns and scores for input relation
		HttpEntity entity1 = new NStringEntity(
				"{\n" +
						"	\"size\" : 20 ,\n" +
						"    \"query\" : {\n" +
						"   \"bool\": {\n"+
						"	\"must\":[\n"+
						"{\n"+
						"    \"match\" : {\n"+
						"	  \"dbpediaRelation\" : \""+evidence.getModel().getPredicate().getLocalName()+"\"\n"+
						"} \n"+
						"} \n"+
						"] \n"+
						"} \n"+
						"} \n"+
						"}", ContentType.APPLICATION_JSON);
		try {
			this.response = restClientobj.performRequest("GET", "/clueweb/pattyPatterns/_search",Collections.singletonMap("pretty", "true"),entity1);
			this.json = EntityUtils.toString(response.getEntity());
			this.rootNode = mapper.readValue(json, JsonNode.class);
			JsonNode hits = rootNode.get("hits");
			JsonNode hitCount = hits.get("total");
			int docCount = Integer.parseInt(hitCount.asText());
			for(int i=0; i<docCount; i++)
			{
				JsonNode document = hits.get(i).get("_source");
				JsonNode pattern = document.get("pattern");
				JsonNode patternStrings = document.get("patternStrings");
				if(proof.getProofPhrase().contains(pattern.asText()))
				{
					for (final JsonNode patternMatch : patternStrings) {
						String normalizedPattern = patternMatch.get("normalizedPattern").asText();
						double patternScore = Double.parseDouble(patternMatch.get("patternScore").asText());
						float swSimilarity = smithWaterman.getSimilarity(normalizedPattern, proof.getNormalizedProofPhrase());
						if ( swSimilarity > smithWatermanSimilarity ) {

							smithWatermanSimilarity = swSimilarity;
							smithWatermanScore = patternScore;
						}

						float qgramsSimil = qgrams.getSimilarity(normalizedPattern, proof.getNormalizedProofPhrase());
						if ( qgramsSimil > qgramsSimilarity ) {

							qgramsSimilarity = qgramsSimil; 
							qgramsSimilarityScore = patternScore;
						}

						float levSimil = lev.getSimilarity(normalizedPattern, proof.getNormalizedProofPhrase());
						if ( levSimil > levSimilarity ) {

							levSimilarity = levSimil; 
							levSimilarityScore = patternScore;
						}

						if ( proof.getProofPhrase().contains(normalizedPattern) ) patternCounter++; 
						if ( proof.getNormalizedProofPhrase().toLowerCase().contains(normalizedPattern) ) patternNormalizedCounter++;

						if(!(org.apache.commons.lang3.StringUtils.substringBetween(proof.getNormalizedProofPhrase().toLowerCase(), proof.getSubject().toLowerCase(), proof.getObject().toLowerCase())==null))
						{
							proofSubString = org.apache.commons.lang3.StringUtils.substringBetween(proof.getNormalizedProofPhrase().toLowerCase(), proof.getSubject().toLowerCase(), proof.getObject().toLowerCase());
							subjectObject = true;
							// see if we can reduce the distance when we have subject and object label more than once
							calculateDistanceSimiliarities(proof, normalizedPattern, patternScore);
						}

						else
						{
							proofSubString = org.apache.commons.lang3.StringUtils.substringBetween(proof.getNormalizedProofPhrase().toLowerCase(), proof.getObject().toLowerCase(), proof.getSubject().toLowerCase());
							subjectObject = false;
							// see if we can reduce the distance when we have subject and object label more than once
							calculateDistanceSimiliarities(proof, normalizedPattern, patternScore);
						}						
					}
				}			
			}
			restClientobj.close();
		}

		catch (Exception e)
		{
			logger.info("Issue with the running Elastic search instance. Please check if the instance is running!");
		}
	}
	
	private static void calculateDistanceSimiliarities(ComplexProof proof, String match, double patternScore)
	{
		while(proofSubString!=null)
		{
			float swSim = smithWaterman.getSimilarity(match, proofSubString.trim());
			if ( swSim > smithWatermanSimilarity ) {
				smithWatermanSimilarity = swSim;
				smithWatermanScore = patternScore;
			}

			float qgramsSim = qgrams.getSimilarity(match, proofSubString.trim());
			if ( qgramsSim > qgramsSimilarity ) {
				qgramsSimilarity = qgramsSim; 
				qgramsSimilarityScore = patternScore;
			}

			float levSim = lev.getSimilarity(match, proofSubString.trim());
			if ( levSim > levSimilarity ) {
				levSimilarity = levSim; 
				levSimilarityScore = patternScore;
			}			
			if(subjectObject)
				proofSubString = org.apache.commons.lang3.StringUtils.substringBetween(proofSubString+proof.getObject().toLowerCase(), proof.getSubject().toLowerCase(), proof.getObject().toLowerCase());
			else
				proofSubString = org.apache.commons.lang3.StringUtils.substringBetween(proofSubString+proof.getSubject().toLowerCase(), proof.getObject().toLowerCase(), proof.getSubject().toLowerCase());
		}
		
		proof.getFeatures().setValue(AbstractFactFeatures.BOA_PATTERN_COUNT, patternCounter);
		proof.getFeatures().setValue(AbstractFactFeatures.BOA_PATTERN_NORMALIZED_COUNT, patternNormalizedCounter);

		proof.getFeatures().setValue(AbstractFactFeatures.LEVENSHTEIN, levSimilarity);
		proof.getFeatures().setValue(AbstractFactFeatures.LEVENSHTEIN_BOA_SCORE, levSimilarityScore);

		proof.getFeatures().setValue(AbstractFactFeatures.QGRAMS, qgramsSimilarity);
		proof.getFeatures().setValue(AbstractFactFeatures.QGRAMS_BOA_SCORE, qgramsSimilarityScore);

		proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN, smithWatermanSimilarity);
		proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN_BOA_SCORE, smithWatermanScore);
	}


	public static void main(String[] args) {

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
