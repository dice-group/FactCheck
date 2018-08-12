package org.dice.factcheck.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.impl.EndOfSentenceCharacterFeature;
import org.aksw.defacto.search.query.MetaQuery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class EndOfSentenceCharacterFeatureTest extends AbstractFactFeatureTest{
	
	private ComplexProof proof;
	private Evidence evidence;
	private double expetcedSentenceCount;
	private Class errorClass;
	private EndOfSentenceCharacterFeature feature = new EndOfSentenceCharacterFeature();
	
	@Parameters
	public static Collection<Object[]> data() {

		List<Object[]> testInput = new ArrayList<Object[]>();
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
				new WebSite(new MetaQuery("Einstein", "awarded", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), 1.0, null});
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "Einstein is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world's most famous equation\"). He recived the Nobel Prize for physics.", 
				"Einstein is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world's most famous equation\"). He received the Nobel prize for physics", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/Albert_Einstein", "en")), 
				new Evidence(testModel), 2.0, null});
		
		testInput.add(new Object[] { null, null, 1.0, NullPointerException.class});
		
		return testInput;
	}
	public EndOfSentenceCharacterFeatureTest(ComplexProof proof, Evidence evidence, double expetcedSentenceCount, Class errorClass) {
		
		this.proof = proof;
		this.evidence = evidence;
		this.expetcedSentenceCount = expetcedSentenceCount;
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
		Assert.assertEquals(this.expetcedSentenceCount, this.proof.getFeatures().value(AbstractFactFeatures.END_OF_SENTENCE_DOT),0.0);
		
	}

}
