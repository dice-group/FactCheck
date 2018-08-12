package org.aksw.defacto.ml.feature.evidence.impl;

import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class NameFeature extends AbstractEvidenceFeature {

    @Override
    public void extractFeature(Evidence evidence) {

        evidence.getFeatures().setValue(AbstractEvidenceFeature.MODEL_NAME, evidence.getModel().getName());
        String uri = evidence.getModel().getPropertyUri().replace("http://dbpedia.org/ontology/", "");
    	if ( uri.equals("office") )
    	    uri = "leaderName";
    	evidence.getFeatures().setValue(AbstractEvidenceFeature.PROPERTY_NAME, uri);
    }
}
