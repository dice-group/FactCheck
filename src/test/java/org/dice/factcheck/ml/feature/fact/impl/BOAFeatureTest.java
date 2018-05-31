package org.dice.factcheck.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.impl.BoaFeature;
import org.aksw.defacto.search.query.MetaQuery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BOAFeatureTest extends AbstractFactFeatureTest{

	private ComplexProof proof;
	private Evidence evidence;
	private double expectedSmithWatermanScore;
	private double expectedLevehnsteinScore;
	private double expectedQgramScore;
	private BoaFeature feature = new BoaFeature();
	private Class errorClass;
	
	@Parameters
	public static Collection<Object[]> data() {
		
		List<Object[]> testInput = new ArrayList<Object[]>();
		
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
				new WebSite(new MetaQuery("Einstein", "awarded", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), 0.928, 0.866, 0.787, null});
		
		// when the tokens between subject and object are large in number 
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "Einstein is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world's most famous equation\"). He recived the Nobel Prize for physics.", 
				"Einstein is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world's most famous equation\"). He received the Nobel prize for physics", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), 1.0, 0.094, 0.137, null});
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "Einstein received Nobel Prize for physics.", "Einstein received Nobel Prize for physics.", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), 1.0, 0.666, 0.666, null});
		
		// when one of the subject/object label is missing
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "He received Nobel Prize for physics.", "He received Nobel Prize for physics.", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), 0.75, 0.277, 0.269, null});
		
		// check for exception
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "Einstein received Nobel Prize for physics.", "Einstein received Nobel Prize for physics.", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				null, 1.0, 0.666, 0.666, NullPointerException.class});
		
		return testInput;
	}
	
	public BOAFeatureTest(ComplexProof proof, Evidence evidence, double expectedSmithWatermanScore, double expectedLevehnsteinScore, double expectedQgramScore, Class errorClass) {
		
		this.proof = proof;
		this.evidence = evidence;
		this.expectedSmithWatermanScore = expectedSmithWatermanScore;
		this.expectedLevehnsteinScore = expectedLevehnsteinScore;
		this.expectedQgramScore = expectedQgramScore;
		this.errorClass = errorClass;
	}

	@Test
	public void test() {		
		
		if(this.errorClass != null)
		{
			try {
				feature.extractFeature(this.proof, this.evidence);
		        Assert.fail("Excpected expection");
		    } catch (NullPointerException e) {
		        Assert.assertTrue(true);
		        return;
		    }
		}
		feature.extractFeature(this.proof, this.evidence);
		Assert.assertEquals(this.expectedSmithWatermanScore, round(this.proof.getFeatures().value(AbstractFactFeatures.SMITH_WATERMAN),3),0.0);
		Assert.assertEquals(this.expectedQgramScore, round(this.proof.getFeatures().value(AbstractFactFeatures.QGRAMS),3),0.0);
		Assert.assertEquals(this.expectedLevehnsteinScore, round(this.proof.getFeatures().value(AbstractFactFeatures.LEVENSHTEIN),3),0.0);
		
	}

}
