package org.dice.factcheck.search.engine.elastic;
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
public class ElasticSearchEngineTest extends AbstractElasticInstance {
	
	private String testIndex;
	private String testDocumentType;
	private static HashMap<String, String> documentFields;
	private static HashMap<Integer, HashMap<String, String>> documents;
	private static MetaQuery query;
	private static Pattern pattern;
	private static Long expectedCount;
	
	
	@Parameters
	public static Collection<Object[]> data() throws IOException {		
		
		documentFields = new HashMap<String, String>();
		documentFields.put("Title", "text");
		documentFields.put("Article", "text");
		documentFields.put("URL", "text");
		documentFields.put("Pagerank", "float");
		Defacto.init();
		startRestClient();
		createIndex("wikipedia-test", "articles", documentFields);
		
		documents = new HashMap<Integer, HashMap<String, String>>();
		HashMap<String, String> documentContent = new HashMap<String, String>();
		documentContent.put("Title", "Albert Einstein");
		documentContent.put("Article", "Einstein was born in Ulm, Germany. He received Nobel prize for physics");
		documentContent.put("URL", "www.albert-einstein.com");
		documentContent.put("Pagerank", "0.35");
		documents.put(1, documentContent);
		
		documentContent = new HashMap<String, String>();
		documentContent.put("Title", "Nobel Prize Leaurates");
		documentContent.put("Article", "Albert Einstein was a German-born theoretical physicist who developed the theory of relativity, one of the two pillars of modern physics (alongside quantum mechanics). "
				+ "His work is also known for its influence on the philosophy of science. "
				+ "He is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world's most famous equation\"). "
				+ "He received the 1921 Nobel Prize for Physics \"for his services to theoretical physics, and especially for his discovery of the law of the photoelectric effect\", a pivotal step in the evolution of quantum theory.");
		documentContent.put("URL", "www.nobel-prize-physics.com");
		documentContent.put("Pagerank", "0.50");
		documents.put(2, documentContent);
		
		documentContent = new HashMap<String, String>();
		documentContent.put("Title", "Henry Dunant");
		documentContent.put("Article", "Henry Dunant was born in France. He received Nobel peace prize");
		documentContent.put("URL", "www.henry-dunant.com");
		documentContent.put("Pagerank", "0.15");
		documents.put(3, documentContent);
		
		documentContent = new HashMap<String, String>();
		documentContent.put("Title", "Max Planck");
		documentContent.put("Article", "Max Planck was born in Geramny. He received Nobel prize for physics");
		documentContent.put("URL", "www.henry-dunant.com");
		documentContent.put("Pagerank", "0.55");
		documents.put(4, documentContent);
		
		List<Object[]> testInput = new ArrayList<Object[]>();
		
		testInput.add(new Object[] {new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), 
				new Pattern("received", "en"), 2});
		
		testInput.add(new Object[] {new MetaQuery("Einstein", "received", "Nobel peace prize", "en", null), 
				new Pattern("received", "en"), 0});
		
		testInput.add(new Object[] {new MetaQuery("Einstein", "??? NONE ???", "Nobel Prize for Physics", "en", null), 
				new Pattern("received", "en"), 2});
		
		return testInput;
	}
	
	public ElasticSearchEngineTest(MetaQuery query, Pattern pattern, int expectedCount) {
		
		this.query = query;
		this.pattern = pattern;
		this.expectedCount = (long) expectedCount;
		
	}
	
	@Test
	public void TestMethod() throws IOException
	{
		ElasticSearchEngine serachEngine = new ElasticSearchEngine(); 
		SearchResult result = serachEngine.query(query, pattern);
		
		Assert.assertEquals(this.expectedCount, result.getTotalHitCount());
	}
	
	public static void indexDocuments(HashMap<Integer, HashMap<String, String>> documents, String indexName, String docType) throws IOException
	{
		for (Map.Entry<Integer, HashMap<String, String>> entry : documents.entrySet()) {
				ObjectMapper mapper = new ObjectMapper();
				ObjectNode node = mapper.createObjectNode();
				node.put("Title", entry.getValue().get("Title"));
				node.put("Article", entry.getValue().get("Article"));
				node.put("URL", entry.getValue().get("URL"));
				node.put("Pagerank", Float.parseFloat(entry.getValue().get("Pagerank")));
				HttpEntity entity = new NStringEntity(
						node.toString(), ContentType.APPLICATION_JSON);
				Response response = client.performRequest("PUT", "/"+indexName+"/"+docType+"/"+entry.getKey(), Collections.singletonMap("pretty", "true"), entity);				
			
		}	
	}

}
