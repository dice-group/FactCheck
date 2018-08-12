package org.dice.factcheck.proof.extract;

import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.query.MetaQuery;

public abstract class TestWebsite {
	
	public static WebSite getWebsite(String websiteText, String subject, String predicate, String Object, String language)
	{
		WebSite website = new WebSite(new MetaQuery(subject, predicate, Object, language, null), "http://example.com");
		website.setText(websiteText);
		website.setLanguage(language);
		website.setPredicate(predicate);
		return website;
	}
	
	public static WebSite getWebsite(String websiteText, String subject, String predicate, String Object, String language, String title)
	{
		WebSite website = new WebSite(new MetaQuery(subject, predicate, Object, language, null), "http://examplewebssite.com");
		website.setText(websiteText);
		website.setLanguage(language);
		website.setPredicate(predicate);
		website.setTitle(title);
		return website;
	}
	
	public static WebSite getWebsite(String websiteText, String subject, String predicate, String Object, String language, float rank, double score, String url)
	{
		WebSite website = new WebSite(new MetaQuery(subject, predicate, Object, language, null), url);
		website.setLanguage(language);
		website.setText(websiteText);
		website.setPredicate(predicate);
		website.setRank(rank);
		website.setScore(score);
		return website;
	}
	
	public static WebSite getWebsite(String subject, String predicate, String Object, String language, float rank, double score, String url)
	{
		WebSite website = new WebSite(new MetaQuery(subject, predicate, Object, language, null), url);
		website.setLanguage(language);
		website.setPredicate(predicate);
		website.setRank(rank);
		website.setScore(score);
		return website;
	}
	
	public static Evidence getEvidence(DefactoModel model, Object[] proofs)
	{
		Evidence evidence = new Evidence(model);
		
		for(int i=0; i<proofs.length; i++)
		{
			ComplexProof proof = (ComplexProof) proofs[i];
			evidence.addComplexProof(proof);
		}
		return evidence;
	}

}
