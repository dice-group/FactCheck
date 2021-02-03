package org.dice.factcheck.ml.feature.evidence.impl;

import java.math.BigDecimal;
import java.util.Arrays;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.model.DefactoModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public abstract class AbstractEvidenceFeatureTest {
	
	public static DefactoModel testModel = loadTestModel();
	
	public static DefactoModel loadTestModel()
	{
		final Model model = ModelFactory.createDefaultModel();
		model.read(AbstractEvidenceFeatureTest.class.getClassLoader().getResourceAsStream("Einstein.ttl"), null,
				"TURTLE");
		Defacto.init();
		DefactoModel defactoModel = new DefactoModel(model, "Einstein Model", true, Arrays.asList("en"));
		return defactoModel;
	}
	
	public static double round(double d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Double.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_FLOOR);       
        return bd.doubleValue();
    }

}
