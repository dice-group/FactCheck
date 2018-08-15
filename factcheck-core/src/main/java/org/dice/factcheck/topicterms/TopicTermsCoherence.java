package org.dice.factcheck.topicterms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.dice.factcheck.topicterms.Word;
import org.aksw.defacto.Defacto;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.ini4j.InvalidFileFormatException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Zafar Syed <zsyed@mail.uni-paderborn.de>
 *
 */
public class TopicTermsCoherence {
	
	private static String ELASTIC_SERVER;
	private static String ELASTIC_PORT;
	private static String NUMBER_OF_TERMS;

	private static Logger logger = Logger.getLogger(TopicTermsCoherence.class);
	public static void main(String[] args) throws InvalidFileFormatException, IOException {
		getTerms("Albert Einstein");
	}


	public static List<Word> getTerms(String label)
	{
		ArrayList<Word> wordList = new ArrayList<Word>();
		if ( Defacto.DEFACTO_CONFIG != null ) {

			ELASTIC_SERVER = Defacto.DEFACTO_CONFIG.getStringSetting("elastic", "SERVER_ADDRESS");
			ELASTIC_PORT = Defacto.DEFACTO_CONFIG.getStringSetting("elastic", "PORT_NUMBER");
			NUMBER_OF_TERMS = Defacto.DEFACTO_CONFIG.getStringSetting("topicTerms", "NUMBER_OF_TERMS");
		}

		try {
			// Query the topic terms for the input label (terms scored using Palmetto) 
			RestClient restClientobj = RestClient.builder(new HttpHost(ELASTIC_SERVER , Integer.parseInt(ELASTIC_PORT), "http")).build();
			HttpEntity entity1 = new NStringEntity(
					"{\n" +
							"	\"size\" : 20 ,\n" +
							"    \"query\" : {\n" +
							"    \"match_phrase\" : {\n"+
							"	  \"Topic\" : {\n" +
							"	\"query\" : \""+label+"\"\n"+
							"} \n"+
							"} \n"+
							"} ,\n"+
							"  \"sort\": [\n"+
							"{\n"+
<<<<<<< HEAD:factcheck-core/src/main/java/org/dice/factcheck/topicterms/TopicTermsCoherence.java
							"    \"C_NPMI\" : {\n" +
=======
							"    \"C_UCI\" : {\n" +
>>>>>>> master:factcheck-core/src/main/java/org/dice/factcheck/topicterms/TopicTermsCoherence.java
							"  \"order\" : \"desc\""+
							"}\n"+
							"}]\n"+
							"}", ContentType.APPLICATION_JSON);
			Response response = restClientobj.performRequest("GET", "/wikipedia/topicterms/_search",Collections.singletonMap("pretty", "true"),entity1);
			String json = EntityUtils.toString(response.getEntity());
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readValue(json, JsonNode.class);
			JsonNode hits = rootNode.get("hits");
			JsonNode hitCount = hits.get("total");
			int docCount = Integer.parseInt(hitCount.asText());
			int numberOfTerms = Integer.parseInt(NUMBER_OF_TERMS);
			// we might want to limit number of terms used
			if(!(docCount<numberOfTerms))
				docCount = numberOfTerms;
			for(int i=0; i<docCount; i++)
			{				
				JsonNode document = hits.get("hits").get(i).get("_source");
				JsonNode Term = document.get("Term");
<<<<<<< HEAD:factcheck-core/src/main/java/org/dice/factcheck/topicterms/TopicTermsCoherence.java
				JsonNode NPMI = document.get("C_NPMI");
=======
				JsonNode NPMI = document.get("C_UCI");
>>>>>>> master:factcheck-core/src/main/java/org/dice/factcheck/topicterms/TopicTermsCoherence.java
				String topicTerm = Term.asText();
				float uciScore = Float.parseFloat(NPMI.asText());
				Word word = new Word(topicTerm, uciScore);
				wordList.add(word);
			}
		}

		catch (Exception e) {

			logger.info("Issue with the running Elastic search instance. Please check if the instance is running!");
			return wordList;
		}
		//System.out.println(wordList.get(12).getWord().toString());
		return wordList;

	}
	
    public static class WordComparator implements Comparator<Word> {
        
        public int compare(Word firstWord, Word secondWord){
            int comparisonResult = 0;

            //Compare the words as strings
            comparisonResult = firstWord.getWord().compareTo(secondWord.getWord());

            //if the words are the same, then sort them with the scores
            if ( comparisonResult == 0 ) {
                comparisonResult = (firstWord.getScore() > secondWord.getScore() ? 1 :
                        (firstWord.getScore() == secondWord.getScore() ? 0 : -1));
            }

            return comparisonResult;
        }
    }

}

