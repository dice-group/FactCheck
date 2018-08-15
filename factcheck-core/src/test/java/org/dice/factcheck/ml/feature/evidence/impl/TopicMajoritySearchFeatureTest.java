package org.dice.factcheck.ml.feature.evidence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.evidence.impl.TopicMajoritySearchFeature;
import org.aksw.defacto.model.DefactoModel;
import org.dice.factcheck.proof.extract.TestWebsite;
import org.dice.factcheck.topicterms.Word;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TopicMajoritySearchFeatureTest extends AbstractEvidenceFeatureTest {

	private Evidence evidence;
	private double expectedMajoritysearchSum;
	private double expectedMajoritySearchMax;
	private TopicMajoritySearchFeature feature = new TopicMajoritySearchFeature();
	private Class errorClass;

	@Parameters
	public static Collection<Object[]> data() {
		
		List<Object[]> testInput = new ArrayList<Object[]>();
		WebSite inputWebsite;
		List<WebSite> websiteList= new ArrayList<WebSite>();
		List<Word> wordList;
		DefactoModel testModel = loadTestModel();
		
		Evidence evidence = new Evidence(testModel);
		inputWebsite = TestWebsite.getWebsite("Einstein was born in Ulm, Germany. He was awarded Nobel Prize for physics.", "Einstein", "received", "Nobel prize in physics", "en", (float)0.45, 0.75, "http://example1.com");
		websiteList.add(inputWebsite);
		evidence.addComplexProof(new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
				inputWebsite));

		inputWebsite = TestWebsite.getWebsite("Einstein was born in Ulm, Germany. He was awarded Nobel Prize for physics"
				+ " for his discovery of law of photo electric effect. He also published paper on general relativity", 
				"Albert Einstein", "received", "Nobel prize for physics", "en", (float)0.55, 0.90, "http://example2.com");

		websiteList.add(inputWebsite);
		evidence.addComplexProof(new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
				inputWebsite));

		wordList = new ArrayList<Word>();
		wordList.add(new Word("Physics", (float)0.52));
		wordList.add(new Word("Nobel Prize", (float)0.50));
		wordList.add(new Word("photo electric", (float)0.55));
		wordList.add(new Word("Germany", (float)0.25));
		wordList.add(new Word("quantum theory", (float)0.35));
		wordList.add(new Word("relativity", (float)0.39));

		evidence.setTopicTerms("en", wordList);
		evidence.addWebSites(new Pattern("received", "en"), websiteList);
		evidence.setTopicTermVectorForWebsites("en");
		evidence.calculateSimilarityMatrix();
		
		//when all the websites contain topic terms
		testInput.add(new Object[] {evidence, 0.69, 1.27, null});
		
		evidence = new Evidence(testModel);
		websiteList= new ArrayList<WebSite>();
		inputWebsite = TestWebsite.getWebsite("Tom Hanks was born in California. He received the academy award in 1993.", "Tom Hanks", "received", "Academy award", "en", (float)0.45, 0.75, "http://example1.com");
		websiteList.add(inputWebsite);
		evidence.addComplexProof(new ComplexProof(loadTestModel(), "Tom Hanks", "Academy award", 
				"Tom Hanks received the academy award.", "Tom Hanks received the academy award.", 
				inputWebsite));

		inputWebsite = TestWebsite.getWebsite("Einstein was born in Ulm, Germany. He was awarded Nobel Prize for physics"
				+ " for his discovery of law of photo electric effect. He also published paper on general relativity", 
				"Albert Einstein", "received", "Nobel prize for physics", "en", (float)0.55, 0.90, "http://example2.com");

		websiteList.add(inputWebsite);
		evidence.addComplexProof(new ComplexProof(loadTestModel(), "Einstein", "Nobel Prize for"
				+ " physics", "Einstein was awarded Nobel prize for physics.", "Einstein was awarded Nobel prize for physics.", 
				inputWebsite));
		evidence.setTopicTerms("en", wordList);
		evidence.addWebSites(new Pattern("received", "en"), websiteList);
		evidence.setTopicTermVectorForWebsites("en");
		evidence.calculateSimilarityMatrix();
		
		//When all the website similarities is zero
		testInput.add(new Object[] {evidence, 0.0, 0.0, null});
		
		evidence = new Evidence(testModel);
		websiteList= new ArrayList<WebSite>();
		inputWebsite = TestWebsite.getWebsite("Tom Hanks was born in California. He received the academy award in 1993.", "Tom Hanks", "received", "Academy award", "en", (float)0.45, 0.75, "http://example1.com");
		websiteList.add(inputWebsite);
		evidence.addComplexProof(new ComplexProof(loadTestModel(), "Tom Hanks", "Academy award", 
				"Tom Hanks received the academy award.", "Tom Hanks received the academy award.", 
				inputWebsite));
		evidence.setTopicTerms("en", wordList);
		evidence.addWebSites(new Pattern("received", "en"), websiteList);
		evidence.setTopicTermVectorForWebsites("en");
		evidence.calculateSimilarityMatrix();
		
		//When none of the websites contain topic terms
		testInput.add(new Object[] {evidence, 0.0, 0.0, null});
		
		//check for exception as well
		testInput.add(new Object[] {null, 0.69, 1.27, NullPointerException.class});

		return testInput;
	}

	public TopicMajoritySearchFeatureTest(Evidence evidence, double expectedMajoritySearchMax, double expectedMajoritysearchSum, Class errorClass) {

		this.evidence = evidence;
		this.expectedMajoritysearchSum = expectedMajoritysearchSum;
		this.expectedMajoritySearchMax = expectedMajoritySearchMax;
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
		Assert.assertEquals(this.expectedMajoritysearchSum, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_MAJORITY_SEARCH_RESULT_SUM),2), 0.0);
		Assert.assertEquals(this.expectedMajoritySearchMax, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_MAJORITY_SEARCH_RESULT_MAX),2), 0.0);

	}

}
