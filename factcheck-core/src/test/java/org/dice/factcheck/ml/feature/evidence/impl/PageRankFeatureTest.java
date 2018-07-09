package org.dice.factcheck.ml.feature.evidence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.evidence.impl.PageRankFeature;
import org.aksw.defacto.model.DefactoModel;
import org.dice.factcheck.proof.extract.TestWebsite;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PageRankFeatureTest extends AbstractEvidenceFeatureTest{
	
	private Evidence evidence;
	private double expectedPageRankMax;
	private double expectedPageRankSum;
	private PageRankFeature feature = new PageRankFeature();
	private Class errorClass;
	
	@Parameters
	public static Collection<Object[]> data() {
		DefactoModel testModel = loadTestModel();
		WebSite inputWebsite;
		List<WebSite> websiteList= new ArrayList<WebSite>();
		List<Object[]> testInput = new ArrayList<Object[]>();
		
		inputWebsite = TestWebsite.getWebsite("Einstein", "received", "Nobel prize in physics", "en", (float)0.45, 0.75, "http://example1.com");			
		websiteList.add(inputWebsite);
		
		inputWebsite = TestWebsite.getWebsite("Albert Einstein", "received", "Nobel prize for physics", "en", (float)0.55, 0.90, "http://example2.com");			
		websiteList.add(inputWebsite);
		
		Evidence evidence = new Evidence(testModel);
		evidence.addWebSites(new Pattern("received", "en"), websiteList);
		
		//when the pageranks are provided. 
		testInput.add(new Object[] {evidence, 0.495, 0.832, null});
		
		// When the evidence is empty
		testInput.add(new Object[] {new Evidence(testModel), 0.0, 0.0, null});
		
		websiteList = new ArrayList<WebSite>();
		
		inputWebsite = TestWebsite.getWebsite("Einstein", "received", "Nobel prize in physics", "en", (float)0.6, 0.2, "http://example3.com");			
		websiteList.add(inputWebsite);
		
		inputWebsite = TestWebsite.getWebsite("Albert Einstein", "received", "Nobel prize for physics", "en", (float)0.25, 0.8, "http://example4.com");			
		websiteList.add(inputWebsite);
		
		evidence = new Evidence(testModel);
		evidence.addWebSites(new Pattern("received", "en"), websiteList);
		
		// Proof features is combined with website score. Check website with high score and poor pagerank
		// contributes more than website with low score and high pagerank
		testInput.add(new Object[] {evidence, 0.2, 0.32, null});
		
		websiteList = new ArrayList<WebSite>();
		
		inputWebsite = TestWebsite.getWebsite("Einstein", "received", "Nobel prize in physics", "en", (float)0.6, 0.2, "http://example3.com");			
		websiteList.add(inputWebsite);
		
		inputWebsite = TestWebsite.getWebsite("Albert Einstein", "received", "Nobel prize for physics", "en", (float)0.0, 0.5, "http://example4.com");			
		websiteList.add(inputWebsite);
		
		evidence = new Evidence(testModel);
		evidence.addWebSites(new Pattern("received", "en"), websiteList);
		
		// Websites having pagrank score 0 does not contribute
		testInput.add(new Object[] {evidence, 0.12, 0.12, null});
		
		// check for exceptions as well
		testInput.add(new Object[] {null, 0.0, 0.0, NullPointerException.class});
		
		return testInput;
	}
	
	public PageRankFeatureTest(Evidence evidence, double expectedPageRankMax, double expectedPageRankSum, Class errorClass) {
		
		this.evidence = evidence;
		this.expectedPageRankMax = expectedPageRankMax;
		this.expectedPageRankSum = expectedPageRankSum;
		this.errorClass = errorClass;
	}

	@Test
	public void test() {		
		
		if(this.errorClass != null)
		{
			try {
				feature.extractFeature(this.evidence);
		        Assert.fail("Excpected expection");
		    } catch (NullPointerException e) {
		        Assert.assertTrue(true);
		        return;
		    }
		}
		feature.extractFeature(this.evidence);
		Assert.assertEquals(this.expectedPageRankMax, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.PAGE_RANK_MAX),3),0.0);
		Assert.assertEquals(this.expectedPageRankSum, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.PAGE_RANK_SUM),3),0.0);
		
	}

}
