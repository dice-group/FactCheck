/**
 * 
 */
package org.aksw.defacto.ml.feature.evidence.impl;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeature;
import org.aksw.sparql.metrics.DatabaseBackedSPARQLEndpointMetrics;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class GoodnessFeature extends AbstractEvidenceFeature {

    private static DatabaseBackedSPARQLEndpointMetrics metric = null;
	private static SparqlEndpoint endpoint = SparqlEndpoint.getEndpointDBpedia();
	
	static {
		
		try {
			if ( Defacto.DEFACTO_CONFIG  == null )
				Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(new File(Defacto.class.getResource("/defacto.ini").getFile())));

			Class.forName("com.mysql.jdbc.Driver");

			String dbHost = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "DBHOST");
			String dbPort = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "PORT");
			String database = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "DATABASE");
			String dbUser = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "DBUSER");
			String pw = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "PASSWORD");

            Connection conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&password=" + pw);
			metric = new DatabaseBackedSPARQLEndpointMetrics(endpoint, "pmi-cache", conn);
		} catch (ClassNotFoundException | IOException | SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
    @Override
    public void extractFeature(Evidence evidence) {
    	
    	String subject = evidence.getModel().getDBpediaSubjectUri();
    	String object = evidence.getModel().getDBpediaObjectUri();
    	
    	double goodness = -1;
    	if ( subject != null && object != null  ) {
    		
    		goodness = metric.getGoodness(
    				new Individual(subject), new ObjectProperty(evidence.getModel().getPropertyUri()), new Individual(object));
    		
    		evidence.getFeatures().setValue(AbstractEvidenceFeature.GOODNESS, goodness);
    	}
    }
    
    
    public static void main(String[] args) {
		
    	ObjectProperty property = new ObjectProperty("http://dbpedia.org/ontology/author");
		Individual subject = new Individual("http://dbpedia.org/resource/The_Da_Vinci_Code");
		Individual object = new Individual("http://dbpedia.org/resource/Dan_Brown");
		
		System.out.println(metric.getGoodness(subject, property, object));
		System.out.println(metric.getGoodness(subject, new ObjectProperty("http://dbpedia.org/ontology/writer"), object));
	}
}