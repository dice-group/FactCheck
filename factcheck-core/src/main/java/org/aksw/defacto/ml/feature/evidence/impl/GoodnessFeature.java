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
import org.apache.jena.base.Sys;
import org.apache.log4j.Logger;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class GoodnessFeature extends AbstractEvidenceFeature {

	//public static OWLDataFactoryImpl owlDataFactory = new OWLDataFactoryImpl();
	private static Logger logger = Logger.getLogger(GoodnessFeature.class);
    private static DatabaseBackedSPARQLEndpointMetrics metric = null;
	private static SparqlEndpoint endpoint;
	
	static {

		try {
			endpoint = new SparqlEndpoint(new URL("https://synthg-fact.dice-research.org/sparql"));
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		try {
			Class.forName("com.mysql.jdbc.Driver");
			logger.info(" read defacto ini file from class org.aksw.defacto.ml.feature.evidence.impl.GoodnessFeature");
			File targetFile = FileReader.read("org.aksw.defacto.ml.feature.evidence.impl.GoodnessFeature","defacto.ini");
			logger.info(".ini file path is : "+ targetFile.getAbsolutePath());
			Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(targetFile));

			//String dbHost = "host.docker.internal";
			String dbHost = "localhost";
			String dbPort = "3306";
			String database = "dbpedia_metrics";
			String dbUser = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "USER");;
            String pw = Defacto.DEFACTO_CONFIG.getStringSetting("mysql", "PASSWORD");


			logger.info(dbHost + ":" + dbPort + "/" + database +" ==> dbUser is : "+ dbUser + " pass is " + pw);
			logger.info("endpoint is :"+endpoint);
			Connection conn = DriverManager.getConnection("jdbc:mysql://" + dbHost + ":" + dbPort + "/" + database + "?" + "user=" + dbUser + "&password=" + pw+"&allowPublicKeyRetrieval=true&useSSL=false");


			metric = new DatabaseBackedSPARQLEndpointMetrics(endpoint, (String) null, conn);
			//metric = new DatabaseBackedSPARQLEndpointMetrics(endpoint, "pmi-cache", conn);
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
    		
    		//goodness = metric.getGoodness(new Individual(subject), new ObjectProperty(evidence.getModel().getPropertyUri()), new Individual(object));
/*			goodness = metric.getGoodness(
					owlDataFactory.getOWLNamedIndividual(IRI.create(subject)),
					owlDataFactory.getOWLObjectProperty(IRI.create(evidence.getModel().getPropertyUri())),
					owlDataFactory.getOWLNamedIndividual(IRI.create(object)));*/

			goodness = metric.getGoodness(subject,evidence.getModel().getPropertyUri().toString(),object);

    		evidence.getFeatures().setValue(AbstractEvidenceFeature.GOODNESS, goodness);
    	}
    	
    }
    
    
    public void main(String[] args) {

/*		OWLObjectProperty property = owlDataFactory.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/author"));
		OWLNamedIndividual subject = owlDataFactory.getOWLNamedIndividual(IRI.create("http://dbpedia.org/resource/The_Da_Vinci_Code"));
		OWLNamedIndividual object = owlDataFactory.getOWLNamedIndividual(IRI.create("http://dbpedia.org/resource/Dan_Brown"));*/

		String property = "http://dbpedia.org/ontology/author";
		String subject = "http://dbpedia.org/resource/The_Da_Vinci_Code";
		String object = "http://dbpedia.org/resource/Dan_Brown";
		
		System.out.println(metric.getGoodness(subject, property, object));
		//System.out.println(metric.getGoodness(subject, owlDataFactory.getOWLObjectProperty(IRI.create("http://dbpedia.org/ontology/writer")), object));
	}
}
