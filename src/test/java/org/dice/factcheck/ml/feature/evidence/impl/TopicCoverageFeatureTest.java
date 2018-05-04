package org.dice.factcheck.ml.feature.evidence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.evidence.impl.TopicCoverageFeature;
import org.dice.factcheck.proof.extract.TestWebsite;
import org.dice.factcheck.topicterms.Word;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
public class TopicCoverageFeatureTest extends AbstractEvidenceFeatureTest {

	private Evidence evidence;
	private double expectedCoverageSum;
	private double expectedCoverageMax;
	private TopicCoverageFeature feature = new TopicCoverageFeature();
	private Class errorClass;

	@Parameters
	public static Collection<Object[]> data() {
		
		List<Object[]> testInput = new ArrayList<Object[]>();
		WebSite inputWebsite;
		List<WebSite> websiteList= new ArrayList<WebSite>();
		List<Word> wordList;
		inputWebsite = TestWebsite.getWebsite("Einstein was born in Ulm, Germany. He was awarded Nobel Prize for physics.", "Einstein", "received", "Nobel prize in physics", "en", (float)0.45, 0.75, "http://example1.com");
		websiteList.add(inputWebsite);

		inputWebsite = TestWebsite.getWebsite("Einstein was born in Ulm, Germany. He was awarded Nobel Prize for physics"
				+ " for his discovery of law of photo electric effect. He also published paper on general relativity", 
				"Albert Einstein", "received", "Nobel prize for physics", "en", (float)0.55, 0.90, "http://example1.com");			
		websiteList.add(inputWebsite);

		wordList = new ArrayList<Word>();
		wordList.add(new Word("Physics", (float)0.52));
		wordList.add(new Word("Nobel Prize", (float)0.50));
		wordList.add(new Word("photo electric", (float)0.55));
		wordList.add(new Word("Germany", (float)0.25));
		wordList.add(new Word("quantum theory", (float)0.35));
		wordList.add(new Word("relativity", (float)0.39));
		Evidence evidence = new Evidence(loadTestModel());
		evidence.setTopicTerms("en", wordList);
		evidence.addWebSites(new Pattern("received", "en"), websiteList);
		evidence.setTopicTermVectorForWebsites("en");
		
		testInput.add(new Object[] {evidence, (float)4.5, (float)6.75});

		return testInput;
	}

	public TopicCoverageFeatureTest(Evidence evidence, float expectedCoverageMax, float expectedCoverageSum) {

		this.evidence = evidence;
		this.expectedCoverageSum = expectedCoverageSum;
		this.expectedCoverageMax = expectedCoverageMax;
	}

	@Test
	public void test() {

		feature.extractFeature(this.evidence);
		Assert.assertEquals(this.expectedCoverageMax, this.evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_COVERAGE_MAX),0.001);
		Assert.assertEquals(this.expectedCoverageSum, this.evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_COVERAGE_SUM),0.002);

	}

}
