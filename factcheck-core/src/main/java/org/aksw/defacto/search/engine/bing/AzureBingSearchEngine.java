package org.aksw.defacto.search.engine.bing;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.billylieurance.azuresearch.AbstractAzureSearchQuery.AZURESEARCH_QUERYTYPE;
import net.billylieurance.azuresearch.AbstractAzureSearchResult;
import net.billylieurance.azuresearch.AzureSearchCompositeQuery;
import net.billylieurance.azuresearch.AzureSearchResultSet;
import net.billylieurance.azuresearch.AzureSearchWebResult;

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
 * Date: 2/6/12
 * Time: 7:11 PM
 * Class BingSearchEngine contains the facilities required to contact Bing search engine to get the search results for a
 * set of keywords
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Mohamed Morsey <morsey@informatik.uni-leipzig.de>
 */
public class AzureBingSearchEngine extends DefaultSearchEngine {

	private String NUMBER_OF_SEARCH_RESULTS;
	private static String BING_API_KEY;
	private static Logger logger =  Logger.getLogger(AzureBingSearchEngine.class);

	public AzureBingSearchEngine() {

		if ( Defacto.DEFACTO_CONFIG != null ) {

			BING_API_KEY = Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "BING_API_KEY");
			NUMBER_OF_SEARCH_RESULTS = Defacto.DEFACTO_CONFIG.getStringSetting("crawl", "NUMBER_OF_SEARCH_RESULTS");
		}
	}

	@Override
	public Long getNumberOfResults(MetaQuery query) {

		return 0L;
	}

	public void main(String[] args) {

		//        MetaQuery query0 = new MetaQuery(String.format("%s|-|%s|-|%s|-|%s", "Obama", "?D? is president of ?R?", "United States", "en"));
		//        MetaQuery query  = new MetaQuery(String.format("%s|-|%s|-|%s|-|%s", "Montebelluna", "?R? Wii version of `` ?D?", "Procter & Gamble", "en"));
		//        MetaQuery query1 = new MetaQuery(String.format("%s|-|%s|-|%s|-|%s", "Gloria Estefan", "??? NONE ???", "Remember Me with Love", "en"));
		//        MetaQuery query2 = new MetaQuery(String.format("%s|-|%s|-|%s|-|%s", "Avram Hershko", "?D? is a component of ?R?", "United States Marine Corps", "en"));

		Defacto.init();

		MetaQuery q = new MetaQuery("Ghostbusters II|-|?D? NONE ?R?|-|Bill Murray|-|fr");
		AzureBingSearchEngine engine = new AzureBingSearchEngine();
		System.out.println(BING_API_KEY);
		System.out.println(engine.query(q, null).getTotalHitCount());
		//        System.out.println(engine.query(query, null).getWebSites().size());

		//        URI uri;
		//        try {
		//            String query = "'Obama' AND 'is president of' AND 'United States'";
		//                uri = new URI("https", "api.datamarket.azure.com", "/Data.ashx/Bing/SearchWeb/v1/Web",
		//                        "Query='"+query+"'", null );
		//                //Bing and java URI disagree about how to represent + in query parameters.  This is what we have to do instead...
		//                uri = new URI(uri.getScheme() + "://" + uri.getAuthority()  + uri.getPath() + "?" + uri.getRawQuery().replace("+", "%2b"));
		//                System.out.println(uri);
		//                
		//         //log.log(Level.WARNING, uri.toString());
		//        } catch (URISyntaxException e1) {
		//                e1.printStackTrace();
		//                return;
		//        }



		//        System.out.println(engine.query(query1, null).getWebSites().size());
		//        System.out.println(engine.query(query2, null).getWebSites().size());
	}



	@Override
	public SearchResult query(MetaQuery query, Pattern pattern) {

		try {

			//            AzureSearchCompositeQuery aq = new AzureSearchCompositeQuery();
			//            aq.setAppid(BING_API_KEY);
			//            aq.setLatitude("47.603450");
			//            aq.setLongitude("-122.329696");
			//            if ( query.getLanguage().equals("en") ) aq.setMarket("en-US");
			//            else if ( query.getLanguage().equals("de") )aq.setMarket("de-DE");
			//            else aq.setMarket("fr-FR");
			//            
			//            aq.setSources(new AZURESEARCH_QUERYTYPE[] { AZURESEARCH_QUERYTYPE.WEB });
			//            
			//            aq.setQuery(this.generateQuery(query));
			//            System.out.println(this.generateQuery(query));
			//            aq.doQuery();
			//            
			//            AzureSearchResultSet<AbstractAzureSearchResult> ars = aq.getQueryResult();
			// query bing and get only the urls and the total hit count back
			List<WebSite> results = new ArrayList<WebSite>();

			RestClient restClientobj = RestClient.builder(new HttpHost("localhost", 9200, "http")).build();
//			HttpEntity entity1 = new NStringEntity(
//					"{\n" +
//							"	\"size\" : 100,\n"+
//							"    \"query\" : {\n" +
//							"    \"match_phrase\" : {\n"+
//							"	 \"Article\" : {\n" +
//							"	\"query\" : "+this.generateQuery(query)+",\n"+
//							"	\"slop\"  : 20 \n"+
//							"} \n"+
//							"} \n"+
//							"} \n"+
//							"}", ContentType.APPLICATION_JSON);
			String subject  = query.getSubjectLabel().replace("&", "and");
	        String property = normalizePredicate(query.getPropertyLabel().trim());
	        String object   = query.getObjectLabel().replace("&", "and");
	        String q1 = String.format("\"%s %s\"", subject, property);
	        if ( query.getPropertyLabel().equals("??? NONE ???") )
	        	q1 = String.format("\"%s BLABLA\"", subject);
	        String q2 = String.format("\"%s\"", object);
	        String q3 = String.format("\"%s %s\"", subject,object);
	        	
			
			HttpEntity entity1 = new NStringEntity(
					 "{\n" +		 
					"    \"query\" : {\n" +
					"	 \"bool\"  : {\n" +
					"	 \"must\"  : [\n" +	
					"	{\n"+
					"    \"match_phrase\" : {\n"+
					"	 \"Article\" : {\n" +
					"	\"query\" : "+q1+",\n"+
					"	\"slop\"  : 20 \n"+
					"} \n"+
					"} \n"+
					"} ,\n"+
					"	{\n"+
					"    \"match_phrase\" : {\n"+
					"	 \"Article\" : {\n" +
					"	\"query\" : "+q2+"\n"+
					"} \n"+
					"} \n"+
					"} ,\n"+
					"	{\n"+
					"    \"match_phrase\" : {\n"+
					"	 \"Article\" : {\n" +
					"	\"query\" : "+q3+",\n"+
					"	\"slop\"  : 80 \n"+
					"} \n"+
					"} \n"+
					"} \n"+
					"] \n"+
					"} \n"+
					"} \n"+
					"}", ContentType.APPLICATION_JSON);
	        
	        
//				HttpEntity entity1 = new NStringEntity(
//				 "{\n" +		 
//				"    \"query\" : {\n" +
//				"	 \"bool\"  : {\n" +
//				"	 \"must\"  : [\n" +	
//				"	{\n"+
//				"    \"match_phrase\" : {\n"+
//				"	 \"Article\" : {\n" +
//				"	\"query\" : "+q1+",\n"+
//				"	\"slop\"  : 10 \n"+
//				"} \n"+
//				"} \n"+
//				"} \n"+
//				"	{\n"+
//				"    \"match_phrase\" : {\n"+
//				"	 \"Article\" : {\n" +
//				"	\"query\" : "+q2+",\n"+
//				"	\"slop\"  : 10 \n"+
//				"} \n"+
//				"} \n"+
//				"} \n"+
//				"] \n"+
//				"} \n"+
//				"} \n"+
//				"}", ContentType.APPLICATION_JSON);
			Response response = restClientobj.performRequest("GET", "/wiki/articles/_search",Collections.singletonMap("pretty", "true"),entity1);
			String json = EntityUtils.toString(response.getEntity());			
			//System.out.println(json);
			ObjectMapper mapper = new ObjectMapper();
			@SuppressWarnings("unchecked")
			JsonNode rootNode = mapper.readValue(json, JsonNode.class);
			JsonNode hits = rootNode.get("hits");
			JsonNode hitCount = hits.get("total");
			int docCount = Integer.parseInt(hitCount.asText());
			for(int i=0; i<docCount; i++)
			{
				JsonNode articleNode = hits.get("hits").get(i).get("_source").get("Article");
				JsonNode articleURLNode = hits.get("hits").get(i).get("_source").get("URL");
				JsonNode articleTitleNode = hits.get("hits").get(i).get("_source").get("Title");
				JsonNode articleID = hits.get("hits").get(i).get("_id");
				String articleText = articleNode.asText();
				String articleId = articleID.asText();
				String articleURL = articleURLNode.asText();
				String articleTitle = articleTitleNode.asText();

				WebSite website = new WebSite(query, articleURL);
				website.setTitle(articleTitle);
				website.setText(articleText);
				website.setRank(i++);
				website.setLanguage(query.getLanguage());
				results.add(website);
			}

			return new DefaultSearchResult(results, new Long(docCount), query, pattern, false);
		}
		catch (Exception e) {

			e.printStackTrace();
			return new DefaultSearchResult(new ArrayList<WebSite>(), 0L, query, pattern, false);
		}
	}
    public String normalizePredicate(String propertyLabel) {
System.out.println(propertyLabel);
        return propertyLabel.replaceAll(",", "").replace("`", "").replace(" 's", "'s").replace("?R?", "").replace("?D?", "").replaceAll(" +", " ").replaceAll("'[^s]", "").replaceAll("&", "and").trim();
    }

	@Override
	public String generateQuery(MetaQuery query) {

		return new BingQuery().generateQuery(query);
	}
}
