package org.dice.factcheck.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.impl.TokenDistanceFeature;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.query.MetaQuery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class TokenDistanceFeatureTest extends AbstractFactFeatureTest{
	
	private ComplexProof proof;
	private Evidence evidence;
	private double tokenDistnace;
	private float averageTokenLength;
	TokenDistanceFeature feature = new TokenDistanceFeature();
	
	@Parameters
	public static Collection<Object[]> data() {
		DefactoModel testModel = loadTestModel();
		List<Object[]> testInput = new ArrayList<Object[]>();
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
				new WebSite(new MetaQuery("Einstein", "awarded", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), 12.0, (float)4.5});
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize in"
				+ " physics", "Einstein is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world's most famous equation\"). He recived the Nobel Prize for physics.", 
				"Einstein is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world's most famous equation\"). He received the Nobel prize for physics", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), 32.0, (float)5.03});
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
				new WebSite(new MetaQuery("Einstein", "awarded", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), 1.0, (float)0.0});		
		
		return testInput;
	}
	public TokenDistanceFeatureTest(ComplexProof proof, Evidence evidence, double tokenDistnace, float averageTokenLength) {
		
		this.proof = proof;
		this.evidence = evidence;
		this.tokenDistnace = tokenDistnace;
		this.averageTokenLength = averageTokenLength;
	}

	@Test
	public void test() {		
		
		feature.extractFeature(this.proof, this.evidence);
		Assert.assertEquals(this.tokenDistnace, this.proof.getFeatures().value(AbstractFactFeatures.TOKEN_DISTANCE),0.0);
		Assert.assertEquals(this.averageTokenLength, this.proof.getFeatures().value(AbstractFactFeatures.AVERAGE_TOKEN_LENGHT),0.002);
		
	}

}
