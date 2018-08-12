package org.dice.factcheck.search.engine.elaticinstance;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.aksw.defacto.Defacto;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class AbstractElasticInstance {
    protected final static int HTTP_TEST_PORT = 9200;
    protected static RestClient client;
    protected static String ELASTIC_SERVER;
    
    @BeforeClass
    public static void startRestClient() {
    	if ( Defacto.DEFACTO_CONFIG != null ) {
    		
    	ELASTIC_SERVER = Defacto.DEFACTO_CONFIG.getStringSetting("elastic", "SERVER_ADDRESS");
    	
    	}
        try {
        	System.out.println(ELASTIC_SERVER);
            client = RestClient.builder(new HttpHost("localhost", HTTP_TEST_PORT)).build();
            Response response = client.performRequest("GET", "/");
            String json = EntityUtils.toString(response.getEntity());
            ObjectMapper mapper = new ObjectMapper();
			JsonNode rootNode = mapper.readValue(json, JsonNode.class);
			JsonNode hits = rootNode.get("tagline");
            
        } catch (IOException e) {
            
            Assert.fail("Elastic search instance is not running!. Please skip test cases related to scripts");
        }
    }

    @AfterClass
    public static void stopRestClient() throws IOException {
        if (client != null) {
            client.close();
            client = null;
        }
        
    }
    
    public static void createIndex(String indexName, String docType, HashMap<String, String> fields) throws IOException
	{
    	Response response = client.performRequest("DELETE", "/"+indexName);
    	System.out.println(response.toString());
    	client.performRequest("PUT", "/"+indexName);
		Iterator<Entry<String, String>> it = fields.entrySet().iterator();
		String indexFields = "";
		while(it.hasNext())
		{
			Entry<String, String> entry = it.next();
			if(it.hasNext())
			{
				indexFields = indexFields + "	 \""+entry.getKey()+"\" : { \"type\" : \""+entry.getValue()+"\" }\n,";
			}
			else
			{
				indexFields = indexFields + "	 \""+entry.getKey()+"\" : { \"type\" : \""+entry.getValue()+"\" }\n";
			}
		}
		System.out.println(indexFields);
		HttpEntity entity1 = new NStringEntity(
				"{\n" +
						"    \""+docType+"\" : {\n" +
						"    \"properties\" : {\n"+
						indexFields +
						"} \n"+
						"} \n"+
						"}", ContentType.APPLICATION_JSON);
		
		client.performRequest("PUT", "/"+indexName+"/_mappings/"+docType, Collections.singletonMap("pretty", "true"), entity1);
	}
}