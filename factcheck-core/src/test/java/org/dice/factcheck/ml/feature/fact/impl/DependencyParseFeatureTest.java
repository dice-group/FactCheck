package org.dice.factcheck.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.search.query.MetaQuery;
import org.dice.factcheck.nlp.stanford.impl.CoreNLPLocalClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DependencyParseFeatureTest extends AbstractFactFeatureTest{
	
	private ComplexProof proof;
	private Evidence evidence;
	private float expectedScore;
	private DependencyParseFeature feature = new DependencyParseFeature();
	private Class errorClass;
	
	@Parameters
	public static Collection<Object[]> data() {
		
		testModel.corenlpClient = new CoreNLPLocalClient();

		List<Object[]> testInput = new ArrayList<Object[]>();
		
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "Einstein received Nobel Prize for physics.", "Einstein received Nobel Prize for physics.", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/wiki/Albert_Einstein", "en")),
				new Evidence(testModel), (float)1.0, null});
		
		// when the subject and predicate are not connected
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
				+ " physics", "After nominated by Einstein, Pauli received Nobel Prize for physics.", "", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/wiki/Albert_Einstein", "en")),
				new Evidence(testModel), (float)0.5, null});
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize", "Einstein's Nobel prize, which he received in 1921.", "", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/wiki/Albert_Einstein", "en")),
				new Evidence(testModel), (float)1.0, null});
		
		// when none of the connections exist
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize", "Tom Hanks received Academy award.", "", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/wiki/Albert_Einstein", "en")),
				new Evidence(testModel), (float)0.0, null});
		
		// cehck for exception
		testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize", "Tom Hanks received Academy award.", "", 
				new WebSite(new MetaQuery("Einstein", "received", "Nobel Prize for Physics", "en", null), "http://en.wikipedia.com/wiki/Albert_Einstein", "en")),
				null, (float)0.0, NullPointerException.class});
		
		return testInput;
	}
	public DependencyParseFeatureTest(ComplexProof proof, Evidence evidence, float expectedScore, Class errorClass) {
		
		this.proof = proof;
		this.evidence = evidence;
		this.expectedScore = expectedScore;
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
		Assert.assertEquals(this.expectedScore, this.proof.getFeatures().value(AbstractFactFeatures.DEPENDENCY_SUBJECT_OBJECT),0.0);
		
	}

}
