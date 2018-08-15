package org.dice.factcheck.ml.feature.fact.impl;

import java.math.BigDecimal;
import java.util.Arrays;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.model.DefactoModel;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public abstract class AbstractFactFeatureTest {
	
	public static DefactoModel testModel = loadTestModel();
	
	public static DefactoModel loadTestModel()
	{
		final Model model = ModelFactory.createDefaultModel();
        model.read(AbstractFactFeatureTest.class.getClassLoader().getResourceAsStream("Einstein.ttl"), null,
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
