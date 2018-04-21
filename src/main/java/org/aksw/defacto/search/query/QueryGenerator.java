package org.aksw.defacto.search.query;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aksw.defacto.Constants;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.model.DefactoModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class QueryGenerator {

    public static final BoaPatternSearcher patternSearcher = new BoaPatternSearcher();
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryGenerator.class);
    private DefactoModel model;
    private RestClient restClientobj;
    
    /**
     * 
     * @param model
     */
    public QueryGenerator(DefactoModel model) {
        
        this.model = model;
        restClientobj = RestClient.builder(new HttpHost("131.234.28.255" , 6060, "http")).build();
    }
    
    /**
     * 
     * @return
     */
    public Map<Pattern,MetaQuery> getSearchEngineQueries(String language){
        
        // and generate the query strings 
        return this.generateSearchQueries(model.getFact(), language);
    }
    
    /**
     * 
     * @param uriToLabels
     * @param fact
     * @return
     */
    private Map<Pattern,MetaQuery> generateSearchQueries(Statement fact, String language){
     
        Map<Pattern,MetaQuery> queryStrings =  new HashMap<Pattern,MetaQuery>();
        String subjectLabel = model.getSubjectLabelNoFallBack(language);//.replaceAll("\\(.+?\\)", "").trim(); 
        String objectLabel  = model.getObjectLabelNoFallBack(language);//.replaceAll("\\(.+?\\)", "").trim();
        
        // we dont have labels in the given language so we generate a foreign query with english labels
        if ( subjectLabel.equals(Constants.NO_LABEL) || objectLabel.equals(Constants.NO_LABEL) ) {
        	
        	subjectLabel = model.getSubjectLabel("en");
        	objectLabel = model.getObjectLabel("en");
        }
        
        // TODO
        // query boa index and generate the meta queries
        System.out.println(fact.getPredicate());
        for (Pattern pattern : patternSearcher.getNaturalLanguageRepresentations(fact.getPredicate().getURI(), language)) {
        	
        	if ( !pattern.getNormalized().trim().isEmpty() ) {
        		
        		MetaQuery metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null);
        		System.out.println(metaQuery);
        		queryStrings.put(pattern, metaQuery);
        	}
        }
        
        
        
        
     // query Elasticsearch index and generate the meta queries for patty patterns
        //System.out.println(fact.getPredicate());
/*        HttpEntity entity1 = new NStringEntity(
				 "{\n" +
						"	\"size\" : 20 ,\n" +
						"    \"query\" : {\n" +
						"   \"bool\": {\n"+
						"	\"must\":[\n"+
						"{\n"+
						"    \"match\" : {\n"+
						"	  \"dbpediaRelation\" : \""+fact.getPredicate()+"\"\n"+
						"} \n"+
						"} \n"+
						"] \n"+
						"} \n"+
						"} \n"+
				"}", ContentType.APPLICATION_JSON);
        Response response=null;
		//System.out.println(entity1.toString());
		try {
			response = restClientobj.performRequest("GET", "/clueweb/pattyPatterns/_search",Collections.singletonMap("pretty", "true"),entity1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String json=null;
		try {
			json = EntityUtils.toString(response.getEntity());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ObjectNode objectNode1;
		ObjectMapper mapper = new ObjectMapper();
		ObjectMapper mapper1 = new ObjectMapper();
		@SuppressWarnings("unchecked")
		JsonNode rootNode=null;
		try {
			rootNode = mapper.readValue(json, JsonNode.class);
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JsonNode hits = rootNode.get("hits");
		JsonNode hitCount = hits.get("total");
		int docCount = Integer.parseInt(hitCount.asText());
		for(int i=0; i<docCount; i++)
		{
			mapper1 = new ObjectMapper();
			objectNode1 = mapper1.createObjectNode();
			JsonNode articleId = hits.get("hits").get(i).get("_id");
			JsonNode articleNode = hits.get("hits").get(i).get("_source").get("dbpediaRelation");
			JsonNode articleURLNode = hits.get("hits").get(i).get("_source").get("pattern");
			MetaQuery metaQuery = new MetaQuery(subjectLabel, articleURLNode.asText(), objectLabel, language, null);
			Pattern pattern = new Pattern(articleURLNode.asText(), "en");
    		System.out.println(metaQuery);
    		queryStrings.put(pattern, metaQuery);
			
		}
		try {
			restClientobj.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        // add one query without any predicate
        //queryStrings.put(new Pattern("??? NONE ???", language), new MetaQuery(subjectLabel, "??? NONE ???", objectLabel, language, null));        
        LOGGER.debug(String.format("Generated %s queries for fact ('%s'): %s", queryStrings.size(), language, fact.asTriple()));
*/        
        return queryStrings;
    }
}
