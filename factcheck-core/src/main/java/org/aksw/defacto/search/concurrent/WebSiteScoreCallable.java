package org.aksw.defacto.search.concurrent;

import java.util.concurrent.Callable;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.fact.SubjectObjectFactSearcher;
import org.apache.log4j.Logger;
import org.dice.factcheck.proof.extract.SubjectObjectProofExtractor;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class WebSiteScoreCallable implements Callable<WebSite> {

    private DefactoModel model;
    private Pattern pattern;
    private WebSite website;
    private Evidence evidence;
    SubjectObjectProofExtractor searcher = new SubjectObjectProofExtractor();
    
    /**
     * 
     * @param website
     * @param evidence
     * @param model
     * @param patterns 
     */
    public WebSiteScoreCallable(WebSite website, Evidence evidence, DefactoModel model) {

        this.website  = website;
        this.model    = model;
        this.evidence = evidence;
    }

    @Override
    public WebSite call() {
        if(searcher==null){
            System.out.println("searcher IS NULL");
            searcher = new SubjectObjectProofExtractor();
        }
        System.out.println("searcher is"+searcher.toString());
        System.out.println(" generate proofs is evidence : "+evidence.toString());
        System.out.println(" generate proofs is website : "+website.toString());
        System.out.println(" generate proofs is model : "+model.toString());

        if (pattern==null){
            System.out.println("pattern IS NULL");
            pattern = new Pattern();
        }

        System.out.println(" generate proofs is pattern : "+pattern.toString());
    	searcher.generateProofs(evidence, website, model, pattern);
        return website;
    }
}
