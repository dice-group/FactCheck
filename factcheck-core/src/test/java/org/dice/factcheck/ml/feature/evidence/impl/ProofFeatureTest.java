package org.dice.factcheck.ml.feature.evidence.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.evidence.impl.ProofFeature;
import org.aksw.defacto.model.DefactoModel;
import org.dice.factcheck.proof.extract.TestWebsite;
import org.junit.Assert;
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
		DefactoModel testModel = loadTestModel();

		Evidence evidence = new Evidence(testModel);
		
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
		
		// evidence contain both positive and negative proofs (negative if proof score < 0.5)
		testInput.add(new Object[] {evidence, 2.0, 0.948, 0.35, null});
		
		evidence = new Evidence(testModel);
		
		proof = new ComplexProof(loadTestModel(), "Einstein", "Nobel Prize for"
				+ " physics", "Einstein nominated Pauli for Nobel prize for physics.", "Einstein nominated Pauli for Nobel prize for physics.", 
				TestWebsite.getWebsite("Einstein", "nominated", "Nobel prize for physics", "en", (float)0.55, 0.90, "http://example1.com"));
		proof.setScore(0.43);
		evidence.addComplexProof(proof);

		proof = new ComplexProof(loadTestModel(), "Einstein", "Nobel Prize for"
				+ " physics", "Einstein was born in Ulm, Germany.", "Einstein was born in Ulm, Germany.", 
				TestWebsite.getWebsite("Einstein", "received", "Nobel prize in physics", "en", (float)0.55, 0.90, "http://example2.com"));
		proof.setScore(0.35);
		evidence.addComplexProof(proof);
		
		// evidence contains only negative proofs
		testInput.add(new Object[] {evidence, 0.0, 0.0, 0.629, null});
		
		evidence = new Evidence(testModel);
		
		proof = new ComplexProof(loadTestModel(), "Einstein", "Nobel Prize for"
				+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
				TestWebsite.getWebsite("Einstein", "awarded", "Nobel prize for physics", "en", (float)0.55, 0.90, "http://example1.com"));
		proof.setScore(0.74);
		evidence.addComplexProof(proof);

		proof = new ComplexProof(loadTestModel(), "Einstein", "Nobel Prize for"
				+ " physics", "Einstein received Nobel prize in physics.", "Einstein received Nobel prize in physics.", 
				TestWebsite.getWebsite("Einstein", "received", "Nobel prize in physics", "en", (float)0.55, 0.90, "http://example2.com"));
		proof.setScore(0.80);
		evidence.addComplexProof(proof);
		
		// evidence contains only positive proofs
		testInput.add(new Object[] {evidence, 2.0, 0.948, 0.0, null});
		
		evidence = new Evidence(testModel);
		
		proof = new ComplexProof(loadTestModel(), "Einstein", "Nobel Prize for"
				+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
				TestWebsite.getWebsite("Einstein", "awarded", "Nobel prize for physics", "en", (float)0.55, 0.90, "http://example1.com"));
		proof.setScore(0.50);
		evidence.addComplexProof(proof);

		proof = new ComplexProof(loadTestModel(), "Einstein", "Nobel Prize for"
				+ " physics", "Einstein was born in Ulm, Germany.", "Einstein was born in Ulm, Germany.", 
				TestWebsite.getWebsite("Einstein", "received", "Nobel prize in physics", "en", (float)0.55, 0.90, "http://example2.com"));
		proof.setScore(0.49);
		evidence.addComplexProof(proof);
		
		// check for boundary cases threshold (negative if score<0.5; else positive)
		testInput.add(new Object[] {evidence, 1.0, 0.5, 0.49, null});
		
		//check for exception as well
		testInput.add(new Object[] {null, 2.0, 0.948, 0.35, NullPointerException.class});

		return testInput;
	}

	public ProofFeatureTest(Evidence evidence, double expectedNumberOfconfirmingProofs, double expectedPositiveScore, double expectedNegativeScore, Class errorClass) {

		this.evidence = evidence;
		this.expectedNumberOfconfirmingProofs = expectedNumberOfconfirmingProofs;
		this.expectedPositiveScore = expectedPositiveScore;
		this.expectedNegativeScore = expectedNegativeScore;
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
		Assert.assertEquals(this.expectedNumberOfconfirmingProofs, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.NUMBER_OF_CONFIRMING_PROOFS),3), 0.0);
		Assert.assertEquals(this.expectedPositiveScore, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.TOTAL_POSITIVES_EVIDENCE_SCORE),3), 0.0);
		Assert.assertEquals(this.expectedNegativeScore, round(this.evidence.getFeatures().value(AbstractEvidenceFeature.TOTAL_NEGATIVES_EVIDENCE_SCORE),3), 0.0);

	}

}
