package org.dice.factcheck.topicterms;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.aksw.defacto.topic.frequency.Word;
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
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Mohamed Morsey <morsey@informatik.uni-leipzig.de>
 */
public class TopicTermsCoherence {

	private static Logger logger = Logger.getLogger(TopicTermsCoherence.class);
	public static void main(String[] args) throws InvalidFileFormatException, IOException {
		getTerms("Albert Einstein");
	}


	public static List<Word> getTerms(String label)
	{
		ArrayList<Word> wordList = new ArrayList<Word>();

		try {

			RestClient restClientobj = RestClient.builder(new HttpHost("131.234.29.15" , 6060, "http")).build();
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
							"    \"Coherence_NPMI\" : {\n" +
							"  \"order\" : \"desc\""+
							"}\n"+
							"}]\n"+
							"}", ContentType.APPLICATION_JSON);
			Response response = restClientobj.performRequest("GET", "/wiki-factcheck/topicterms/_search",Collections.singletonMap("pretty", "true"),entity1);
			String json = EntityUtils.toString(response.getEntity());
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			JsonNode rootNode = mapper.readValue(json, JsonNode.class);
			JsonNode hits = rootNode.get("hits");
			JsonNode hitCount = hits.get("total");
			int docCount = Integer.parseInt(hitCount.asText());
			if(!(docCount<20))
				docCount = 20;
			for(int i=0; i<docCount; i++)
			{				
				JsonNode document = hits.get("hits").get(i).get("_source");
				JsonNode Term = document.get("Term");
				JsonNode UCI = document.get("Coherence_UCI");
				String topicTerm = Term.asText();
				float uciScore = Float.parseFloat(UCI.asText());
				Word word = new Word(topicTerm, uciScore);
				wordList.add(word);
			}
		}

		catch (Exception e) {

			e.printStackTrace();
		}
		//System.out.println(wordList.get(12).getWord().toString());
		return wordList;

	}

}

