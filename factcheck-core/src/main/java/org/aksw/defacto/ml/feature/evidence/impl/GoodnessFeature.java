/**
 * 
 */
package org.aksw.defacto.ml.feature.evidence.impl;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.util.FileReader;
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
			Class.forName("com.mysql.jdbc.Driver");
			File targetFile = FileReader.read("org.aksw.defacto.ml.feature.evidence.impl.GoodnessFeature","defacto.ini");
			Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(targetFile));

			String dbHost = "localhost";
			String dbPort = "3306";
			String database = "dbpedia_metrics";
			String dbUser = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "USER");;
            String pw = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "PASSWORD");


			//System.out.println(dbHost + ":" + dbPort + "/" + database +" ==> dbUser is : "+ dbUser + " pass is " + pw);
            Connection conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&password=" + pw+"&useSSL=false");
			System.out.println ("endpoint is :"+endpoint);
           // metric = new DatabaseBackedSPARQLEndpointMetrics(endpoint, "pmi-cache", conn);
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidFileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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