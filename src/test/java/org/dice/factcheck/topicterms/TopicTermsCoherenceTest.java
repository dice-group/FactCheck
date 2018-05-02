package org.dice.factcheck.topicterms;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.result.SearchResult;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.dice.factcheck.search.engine.elaticinstance.AbstractElasticInstance;
import org.elasticsearch.client.Response;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RunWith(Parameterized.class)
public class TopicTermsCoherenceTest extends AbstractElasticInstance {
	
	private String testIndex;
	private String testDocumentType;
	private static HashMap<String, String> documentFields;
	private static HashMap<Integer, HashMap<String, String>> documents;
	private String label;
	private static int expectedNumberOfTerms;
	private TopicTermsCoherence topicTerms = new TopicTermsCoherence();
	
	
	@Parameters
	public static Collection<Object[]> data() throws IOException {		
		
		documentFields = new HashMap<String, String>();
		documentFields.put("Topic", "text");
		documentFields.put("Term", "text");
		documentFields.put("Coherence_NPMI", "float");
		
		
		//Get the configuration
		Defacto.init();
		
		// Check if test server is running
		startRestClient();
		 
		//Create test index and add documents
		createIndex("wikipedia-test", "topicTerms", documentFields);
		
		documents = new HashMap<Integer, HashMap<String, String>>();
		HashMap<String, String> documentContent = new HashMap<String, String>();
		documentContent.put("Topic", "Einstein");
		documentContent.put("Term", "Physics");
		documentContent.put("Coherence_NPMI", "0.42");
		documents.put(1, documentContent);
		
		documentContent = new HashMap<String, String>();
		documentContent.put("Topic", "Einstein");
		documentContent.put("Term", "Nobel Prize");
		documentContent.put("Coherence_NPMI", "0.35");
		documents.put(2, documentContent);
		
		documentContent = new HashMap<String, String>();
		documentContent.put("Topic", "Einstein");
		documentContent.put("Term", "Germany");
		documentContent.put("Coherence_NPMI", "0.30");
		documents.put(3, documentContent);
		
		documentContent = new HashMap<String, String>();
		documentContent.put("Topic", "Einstein");
		documentContent.put("Term", "photo-electric effect");
		documentContent.put("Coherence_NPMI", "0.35");
		documents.put(4, documentContent);
		
		documentContent = new HashMap<String, String>();
		documentContent.put("Topic", "Dunant");
		documentContent.put("Term", "red cross");
		documentContent.put("Coherence_NPMI", "0.45");
		documents.put(4, documentContent);
		
		indexDocuments(documents, "wikipedia-test", "topicTerms");
		
		// Add test input parameters for testing
		List<Object[]> testInput = new ArrayList<Object[]>();
		
		testInput.add(new Object[] {"Einstein", 4});
		testInput.add(new Object[] {"Dunant", 1});
		
		
		return testInput;
	}
	
	public TopicTermsCoherenceTest(String label, int expectedNumberOfTerms) {
		
		this.label = label;
		this.expectedNumberOfTerms = expectedNumberOfTerms;
	}
	
	@Test
	public void TestMethod() throws IOException
	{
		Assert.assertEquals(expectedNumberOfTerms, topicTerms.getTerms(this.label).size());

	}
	
	public static void indexDocuments(HashMap<Integer, HashMap<String, String>> documents, String indexName, String docType) throws IOException
	{
		for (Map.Entry<Integer, HashMap<String, String>> entry : documents.entrySet()) {
				ObjectMapper mapper = new ObjectMapper();
				ObjectNode node = mapper.createObjectNode();
				node.put("Topic", entry.getValue().get("Topic"));
				node.put("Term", entry.getValue().get("Term"));
				node.put("Coherence_NPMI", Float.parseFloat(entry.getValue().get("Coherence_NPMI")));
				HttpEntity entity = new NStringEntity(
						node.toString(), ContentType.APPLICATION_JSON);
				Response response = client.performRequest("PUT", "/"+indexName+"/"+docType+"/"+entry.getKey(), Collections.singletonMap("pretty", "true"), entity);				
			
		}	
	}

}
