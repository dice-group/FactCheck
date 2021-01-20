package org.dice.factcheck.search.engine.elastic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.search.engine.DefaultSearchEngine;
import org.aksw.defacto.search.query.BingQuery;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.DefaultSearchResult;
import org.aksw.defacto.search.result.SearchResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class ElasticSearchEngine provides functionality to query the Elastic search instance hosting the ClueWeb dataset
 *
 * 
 * @author Zafar Habeeb Syed <zsyed@mail.uni-paderborn.de>
 */
public class ElasticSearchEngine extends DefaultSearchEngine {

	private static String NUMBER_OF_SEARCH_RESULTS;
	private static String ELASTIC_SERVER;
	private static Logger logger =  Logger.getLogger(ElasticSearchEngine.class);
	private static String ELASTIC_PORT;
	private RestClient restClientobj;

	public ElasticSearchEngine() {
	}
	
	public static void init()
	{
		if ( Defacto.DEFACTO_CONFIG != null ) {

			ELASTIC_SERVER = Defacto.DEFACTO_CONFIG.getStringSetting("elastic", "SERVER_ADDRESS");
			ELASTIC_PORT = Defacto.DEFACTO_CONFIG.getStringSetting("elastic", "PORT_NUMBER");
			NUMBER_OF_SEARCH_RESULTS = Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "NUMBER_OF_SEARCH_RESULTS");
		}
	}

	@Override
	public Long getNumberOfResults(MetaQuery query) {

		return 0L;
	}

	@Override
	public SearchResult query(MetaQuery query, Pattern pattern) {

		try {
			List<WebSite> results = new ArrayList<WebSite>();

			this.restClientobj = RestClient.builder(new HttpHost(ELASTIC_SERVER , Integer.parseInt(ELASTIC_PORT), "http")).build();
			String subject  = query.getSubjectLabel().replace("&", "and");
			String property = normalizePredicate(query.getPropertyLabel().trim());
			String object   = query.getObjectLabel().replace("&", "and");
			String q1 = "\""+subject+" "+property+" "+object+"\"";
			if ( query.getPropertyLabel().equals("??? NONE ???") )
				q1 = "\""+subject+" "+object+"\"";
			HttpEntity entity1 = new NStringEntity(
					"{\n" +
							"	\"size\" : 500 ,\n" +
							"    \"query\" : {\n" +
							"    \"match_phrase\" : {\n"+
							"	 \"text\" : {\n" +
							"	\"query\" : "+q1+",\n"+
							"	\"slop\"  : 50 \n"+
							"} \n"+
							"} \n"+
							"} \n"+
							"}", ContentType.APPLICATION_JSON);
			String index = Defacto.DEFACTO_CONFIG.getStringSetting("elastic", "INDEX");
			String mapping = Defacto.DEFACTO_CONFIG.getStringSetting("elastic", "MAPPING");

			Response response = restClientobj.performRequest("GET", "/"+index+"/"+mapping+"/_search",Collections.singletonMap("pretty", "true"),entity1);
			String json = EntityUtils.toString(response.getEntity());			
			//System.out.println(json);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readValue(json, JsonNode.class);
			JsonNode hits = rootNode.get("hits");
			JsonNode hitCount = hits.get("total");
			int docCount = 0;
			if(hitCount.has("value")) {
				docCount = Integer.parseInt(hitCount.get("value").asText());
			}else{
				//TODO check it is correct
					docCount = Integer.parseInt(hitCount.asText());
			}
			int number_of_search_results = Integer.parseInt(NUMBER_OF_SEARCH_RESULTS);
			if(!(docCount<number_of_search_results))
				docCount = number_of_search_results;
			//System.out.println(docCount);
			for(int i=0; i<docCount; i++)
			{
				JsonNode document = hits.get("hits").get(i).get("_source");
				JsonNode articleNode = document.get("text");
				JsonNode articleTitleNode = document.get("title");
				JsonNode pagerank = document.get("popularity_score");
				String articleText = articleNode.asText();
				String articleURL = "https://en.wikipedia.org/wiki/"+articleTitleNode.asText();
				String articleTitle = articleTitleNode.asText();
				WebSite website = new WebSite(query, articleURL);
				website.setTitle(articleTitle);
				website.setText(articleText);
				website.setRank(Float.parseFloat(pagerank.asText()));
				website.setLanguage(query.getLanguage());
				website.setPredicate(property);
				results.add(website);
			}
			return new DefaultSearchResult(results, new Long(docCount), query, pattern, false);
		}
		catch (Exception e) {

			logger.info("Issue with the running Elastic search instance. Please check if the instance is running! "+e.getMessage());
			return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, pattern, false);
		}
		
		finally {
			//Close the connection
			try {
				
				this.restClientobj.close();
				
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
	}
	public String normalizePredicate(String propertyLabel) {
		//System.out.println(propertyLabel);
		return propertyLabel.replaceAll(",", "").replace("`", "").replace(" 's", "'s").replace("?R?", "").replace("?D?", "").replaceAll(" +", " ").replaceAll("'[^s]", "").replaceAll("&", "and").trim();
	}

	@Override
	public String generateQuery(MetaQuery query) {

		return new BingQuery().generateQuery(query);
	}
}

