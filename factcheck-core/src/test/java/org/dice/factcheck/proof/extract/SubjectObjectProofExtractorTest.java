package org.dice.factcheck.proof.extract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.dice.factcheck.nlp.stanford.impl.CoreNLPLocalClient;
import org.dice.factcheck.ml.feature.evidence.impl.AbstractEvidenceFeatureTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class SubjectObjectProofExtractorTest extends AbstractEvidenceFeatureTest{
	
	private Evidence expectedEvidence;
	private Evidence actualEvidence;
	private WebSite website;
	private Pattern pattern;
	private DefactoModel model;
	private SubjectObjectProofExtractor proofExtractor = new SubjectObjectProofExtractor();
	
	@Parameters
	public static Collection<Object[]> data() {
		
		testModel.corenlpClient = new CoreNLPLocalClient();

		List<Object[]> testInput = new ArrayList<Object[]>();
		WebSite inputWebsite;
		Object[] proofs;
		
		
		inputWebsite = TestWebsite.getWebsite("Einstein was born in Ulm. He received Nobel prize in physics.", "Einstein", "received", "Nobel prize in physics", "en");
		
		proofs = new Object[] {
				new ComplexProof(testModel, "Einstein", "Nobel Prize in physics", "Einstein received Nobel prize in physics .", "", inputWebsite)
		};		
		
		testInput.add(new Object[] { new Evidence(testModel), inputWebsite, testModel, new Pattern("received", "en"), TestWebsite.getEvidence(testModel, proofs)});
		
		inputWebsite = TestWebsite.getWebsite("Albert Einstein was a German-born theoretical physicist who developed the theory of relativity, one of the two pillars of modern physics (alongside quantum mechanics). "
				+ "His work is also known for its influence on the philosophy of science. "
				+ "He is best known by the general public for his mass–energy equivalence formula E = mc2 (which has been dubbed \"the world's most famous equation\"). "
				+ "He received the 1921 Nobel Prize in Physics \"for his services to theoretical physics, and especially for his discovery of the law of the photoelectric effect\", a pivotal step in the evolution of quantum theory.", "Einstein", "received", "Nobel prize in physics", "en");
		
		proofs = new Object[] {
				new ComplexProof(testModel, "Einstein", "Nobel Prize in physics", "Albert Einstein received the 1921 Nobel Prize in Physics `` for Albert Einstein services to theoretical physics , and especially for Albert Einstein discovery of the "
						+ "law of the photoelectric effect '' , a pivotal step in the evolution of quantum theory .", "", inputWebsite)
		};
		
		// when the proof phrase spans across more sentences
		testInput.add(new Object[] { new Evidence(testModel), inputWebsite, testModel, new Pattern("received", "en"), TestWebsite.getEvidence(testModel, proofs)});
		
		inputWebsite = TestWebsite.getWebsite("Einstein won Nobel Prize for physics. He was born in Ulm, Germany. Albert Einstein was awarded Nobel prize in physics.", "Einstein", "received", "Nobel prize in physics", "en");
		
		proofs = new Object[] {
				
				new ComplexProof(testModel, "Einstein", "Nobel Prize in physics", "Einstein won Nobel Prize for physics .", "", inputWebsite),
				new ComplexProof(testModel, "Einstein", "Nobel Prize in physics", "Albert Einstein was awarded Nobel prize in physics .", "", inputWebsite)
		};
		
		// when we have multiple potential proof phrases in a website
		testInput.add(new Object[] { new Evidence(testModel), inputWebsite, testModel, new Pattern("received", "en"), TestWebsite.getEvidence(testModel, proofs)});
		
		return testInput;
	}
	
	public SubjectObjectProofExtractorTest(Evidence actualEvidence, WebSite website, DefactoModel model, Pattern pattern, Evidence expectedEvidence) {
		
		this.actualEvidence = actualEvidence;
		this.website = website;
		this.model = model;
		this.pattern = pattern;
		this.expectedEvidence = expectedEvidence;
	}
	
	@Test
	public void test() {
		
		this.proofExtractor.generateProofs(this.actualEvidence, this.website, this.model, this.pattern);
		// check if number of proofs expected are equal to number of proofs extracted
		Assert.assertEquals(this.expectedEvidence.getComplexProofs().size(), this.actualEvidence.getComplexProofs().size());
		
		// Also check if the expected proof phrase strings are same as proof phrases returned
		
		Iterator<ComplexProof> actualProofIterator = this.actualEvidence.getComplexProofs().iterator();
		Iterator<ComplexProof> expectedProofIterator = this.expectedEvidence.getComplexProofs().iterator();
		
		//Assert.assertEquals(this.actualEvidence.getComplexProofs(), this.expectedEvidence.getComplexProofs());
		while(expectedProofIterator.hasNext() || actualProofIterator.hasNext())
		{
			Assert.assertEquals(expectedProofIterator.next().getProofPhrase(), 
					actualProofIterator.next().getProofPhrase());
		}
	}

}
