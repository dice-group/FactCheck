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
		
		testInput.add(new Object[] {evidence, 0.495, 0.832});
		
		testInput.add(new Object[] {new Evidence(testModel), 0.0, 0.0});
		
		return testInput;
	}
	
	public PageRankFeatureTest(Evidence evidence, double expectedPageRankMax, double expectedPageRankSum) {
		
		this.evidence = evidence;
		this.expectedPageRankMax = expectedPageRankMax;
		this.expectedPageRankSum = expectedPageRankSum;
	}

	@Test
	public void test() {		

		feature.extractFeature(this.evidence);
		Assert.assertEquals(this.expectedPageRankMax, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.PAGE_RANK_MAX),3),0.0);
		Assert.assertEquals(this.expectedPageRankSum, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.PAGE_RANK_SUM),3),0.0);
		
	}

}
