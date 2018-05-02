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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class BOAFeatureTest extends AbstractFactFeatureTest{

	private ComplexProof proof;
	private Evidence evidence;
	private float expectedSmithWatermanScore;
	private float expectedLevehnsteinScore;
	private float expectedQgramScore;
	private BoaFeature feature;;
	
	@Parameters
	public static Collection<Object[]> data() {
		
		List<Object[]> testInput = new ArrayList<Object[]>();
		
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
				new WebSite(new MetaQuery("Einstein", "awarded", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), (float)1.0, (float)0.666, (float)0.666});
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "Einstein is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world's most famous equation\"). He recived the Nobel Prize for physics.", 
				"Einstein is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world's most famous equation\"). He received the Nobel prize for physics", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), (float)1.0, (float)0.666, (float)0.666});
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "Einstein received Nobel Prize for physics.", "Einstein received Nobel Prize for physics.", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), (float)1.0, (float)0.666, (float)0.666});
		
		return testInput;
	}
	public BOAFeatureTest(ComplexProof proof, Evidence evidence, float expectedSmithWatermanScore, float expectedLevehnsteinScore, float expectedQgramScore) {
		
		this.feature = new BoaFeature();
		this.proof = proof;
		this.evidence = evidence;
		this.expectedSmithWatermanScore = expectedSmithWatermanScore;
		this.expectedLevehnsteinScore = expectedLevehnsteinScore;
		this.expectedQgramScore = expectedQgramScore;
	}

	@Test
	public void test() {		
		
		feature.extractFeature(this.proof, this.evidence);
		System.out.println(this.proof.getFeatures().value(AbstractFactFeatures.SMITH_WATERMAN));
		System.out.println(this.proof.getFeatures().value(AbstractFactFeatures.LEVENSHTEIN));
		System.out.println(this.proof.getFeatures().value(AbstractFactFeatures.QGRAMS));
		//Assert.assertEquals(this.expectedScore, this.proof.getFeatures().value(AbstractFactFeatures.DEPENDENCY_SUBJECT_OBJECT),0.0);
		
	}

}
