package org.dice.factcheck.ml.feature.fact.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.impl.PageTitleFeature;
import org.aksw.defacto.model.DefactoModel;
import org.dice.factcheck.proof.extract.TestWebsite;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class PageTitleFeatureTest extends AbstractFactFeatureTest {
		
		private ComplexProof proof;
		private Evidence evidence;
		private double expectedPageSubjectscore;
		private double expectedPageObjectscore;
		private PageTitleFeature feature = new PageTitleFeature();
		private Class errorClass;
		
		@Parameters
		public static Collection<Object[]> data() {
			DefactoModel testModel = loadTestModel();
			WebSite inputWebsite;
			List<Object[]> testInput = new ArrayList<Object[]>();
			
			inputWebsite = TestWebsite.getWebsite("Einstein was born in Ulm. He received Nobel prize in physics.", "Einstein", "received", "Nobel prize in physics", "en", "List of winners - Nobel Prize for physics");			
			
			testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
					+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
					inputWebsite), new Evidence(testModel), 1.0, 0.333, null});
			
			inputWebsite = TestWebsite.getWebsite("Einstein was born in Ulm. He received Nobel prize in physics.", "Einstein", "received", "Nobel prize in physics", "en", null);
			testInput.add(new Object[] { new ComplexProof(testModel, "Einstein", "Nobel Prize for"
					+ " physics", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", "The 1921 Nobel prize for physics was awarded to Einstein in 1922.", 
					inputWebsite),	new Evidence(testModel), 0.142, 1.0, NullPointerException.class});			
			
			return testInput;
		}
		
		public PageTitleFeatureTest(ComplexProof proof, Evidence evidence, double expectedPageObjectscore, double expectedPageSubjectscore, Class errorClass) {
			
			this.proof = proof;
			this.evidence = evidence;
			this.expectedPageObjectscore = expectedPageObjectscore;
			this.expectedPageSubjectscore = expectedPageSubjectscore;
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
			Assert.assertEquals(this.expectedPageObjectscore, round(this.proof.getFeatures().value(AbstractFactFeatures.PAGE_TITLE_OBJECT),3) ,0.0);
			Assert.assertEquals(this.expectedPageSubjectscore, round(this.proof.getFeatures().value(AbstractFactFeatures.PAGE_TITLE_SUBJECT),3) ,0.0);
		}

}
