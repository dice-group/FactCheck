/**
 * 
 */
package org.aksw.defacto.ml.feature.fact.impl;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
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
import org.aksw.defacto.search.query.MetaQuery;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

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

		RestClient restClientobj = RestClient.builder(new HttpHost("131.234.28.255" , 6060, "http")).build();
		//System.out.println(evidence.getModel().getPredicate().getLocalName());
		proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN_BOA_SCORE, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.SMITH_WATERMAN, 0);

		proof.getFeatures().setValue(AbstractFactFeatures.QGRAMS_BOA_SCORE, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.QGRAMS, 0);

		proof.getFeatures().setValue(AbstractFactFeatures.LEVENSHTEIN_BOA_SCORE, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.LEVENSHTEIN, 0);

		proof.getFeatures().setValue(AbstractFactFeatures.BOA_PATTERN_COUNT, 0);
		proof.getFeatures().setValue(AbstractFactFeatures.BOA_PATTERN_NORMALIZED_COUNT, 0);

		if ( proof.getProofPhrase().trim().isEmpty() ) return; 

		//List<Pattern> patterns = searcher.querySolrIndex(evidence.getModel().getPropertyUri(), 20, 0, proof.getLanguage());

		float smithWatermanSimilarity = 0f;
		float qgramsSimilarity = 0f;
		float levSimilarity = 0f;
		double smithWatermanScore = 0.00;
		double qgramsSimilarityScore = 0.00;
		double levSimilarityScore = 0.00;
		int patternCounter = 0, patternNormalizedCounter = 0;
		String patternString = "";
		boolean subjectObject = false;

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
		Response response=null;
		String json=null;
		JsonNode rootNode=null;
		ObjectMapper mapper = new ObjectMapper();
		try {
			response = restClientobj.performRequest("GET", "/clueweb/pattyPatterns/_search",Collections.singletonMap("pretty", "true"),entity1);
			json = EntityUtils.toString(response.getEntity());
			rootNode = mapper.readValue(json, JsonNode.class);
		} 

		catch (IOException e) {

		}

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
					String match = patternMatch.get("normalizedPattern").asText();
					double patternScore = Double.parseDouble(patternMatch.get("patternScore").asText());
					float swSimilarity = smithWaterman.getSimilarity(match, proof.getNormalizedProofPhrase());
					if ( swSimilarity > smithWatermanSimilarity ) {

						smithWatermanSimilarity = swSimilarity;
						smithWatermanScore = patternScore;
					}

					float qgramsSimil = qgrams.getSimilarity(match, proof.getNormalizedProofPhrase());
					if ( qgramsSimil > qgramsSimilarity ) {

						qgramsSimilarity = qgramsSimil; 
						qgramsSimilarityScore = patternScore;
					}

					float levSimil = lev.getSimilarity(match, proof.getNormalizedProofPhrase());
					if ( levSimil > levSimilarity ) {

						levSimilarity = levSimil; 
						levSimilarityScore = patternScore;
					}

					if ( proof.getProofPhrase().contains(match) ) patternCounter++; 
					if ( proof.getNormalizedProofPhrase().toLowerCase().contains(match) ) patternNormalizedCounter++;

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

						float swSim = smithWaterman.getSimilarity(match, patternString.trim());
						if ( swSim > smithWatermanSimilarity ) {

							smithWatermanSimilarity = swSim;
							smithWatermanScore = patternScore;
						}

						float qgramsSim = qgrams.getSimilarity(match, patternString.trim());
						if ( qgramsSim > qgramsSimilarity ) {

							qgramsSimilarity = qgramsSim; 
							qgramsSimilarityScore = patternScore;
						}

						float levSim = lev.getSimilarity(match, patternString.trim());
						if ( levSim > levSimilarity ) {

							levSimilarity = levSim; 
							levSimilarityScore = patternScore;
						}			
						if(subjectObject)
							patternString = org.apache.commons.lang3.StringUtils.substringBetween(patternString+proof.getObject().toLowerCase(), proof.getSubject().toLowerCase(), proof.getObject().toLowerCase());
						else
							patternString = org.apache.commons.lang3.StringUtils.substringBetween(patternString+proof.getSubject().toLowerCase(), proof.getObject().toLowerCase(), proof.getSubject().toLowerCase());

					}

				}


			}
			try {
				restClientobj.close();
			} catch (IOException e) {
				
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
