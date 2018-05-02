package org.dice.factcheck.ml.feature.evidence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.evidence.impl.ProofFeature;
import org.dice.factcheck.proof.extract.TestWebsite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ProofFeatureTest extends AbstractEvidenceFeatureTest {

	private Evidence evidence;
	private double expectedNumberOfconfirmingProofs;
	private double expectedPositiveScore;
	private double expectedNegativeScore;
	private ProofFeature feature = new ProofFeature();
	private Class errorClass;

	@Parameters
	public static Collection<Object[]> data() {
		
		List<Object[]> testInput = new ArrayList<Object[]>();
		ComplexProof proof;

		Evidence evidence = new Evidence(loadTestModel());
		
		proof = new ComplexProof(loadTestModel(), "Einstein", "Nobel Prize for"
				+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
				TestWebsite.getWebsite("Einstein", "awarded", "Nobel prize for physics", "en", (float)0.55, 0.90, "http://example1.com"));
		proof.setScore(0.74);
		evidence.addComplexProof(proof);

		proof = new ComplexProof(loadTestModel(), "Einstein", "Nobel Prize for"
				+ " physics", "Einstein was born in Ulm, Germany.", "Einstein was born in Ulm, Germany.", 
				TestWebsite.getWebsite("Einstein", "received", "Nobel prize in physics", "en", (float)0.55, 0.90, "http://example2.com"));
		proof.setScore(0.35);
		evidence.addComplexProof(proof);
		
		proof = new ComplexProof(loadTestModel(), "Einstein", "Nobel Prize for"
				+ " physics", "Einstein received Nobel prize in physics.", "Einstein received Nobel prize in physics.", 
				TestWebsite.getWebsite("Einstein", "received", "Nobel prize for physics", "en", (float)0.55, 0.90, "http://example3.com"));
		proof.setScore(0.80);
		evidence.addComplexProof(proof);
		
		testInput.add(new Object[] {evidence, 2, 1.27, 1.27});

		return testInput;
	}

	public ProofFeatureTest(Evidence evidence, double expectedNumberOfconfirmingProofs, double expectedPositiveScore, double expectedNegativeScore) {

		this.evidence = evidence;
		this.expectedNumberOfconfirmingProofs = expectedNumberOfconfirmingProofs;
		this.expectedPositiveScore = expectedPositiveScore;
		this.expectedNegativeScore = expectedNegativeScore;
	}

	@Test
	public void test() {

		feature.extractFeature(this.evidence);
		/*Assert.assertEquals(this.expectedMajoritysearchSum, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_MAJORITY_SEARCH_RESULT_SUM),2), 0.0);
		Assert.assertEquals(this.expectedMajoritySearchMax, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.TOPIC_MAJORITY_SEARCH_RESULT_MAX),2), 0.0);*/

	}

}
