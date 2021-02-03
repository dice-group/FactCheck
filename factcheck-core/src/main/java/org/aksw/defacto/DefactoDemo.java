package org.aksw.defacto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.model.DefactoModel;
import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.log4j.Logger;
import org.ini4j.InvalidFileFormatException;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class DefactoDemo {

    private static Logger logger = Logger.getLogger(DefactoDemo.class);
    
    /**
     * @param args
     * @throws IOException 
     * @throws InvalidFileFormatException 
     */
    public static void main(String[] args) throws InvalidFileFormatException, IOException {

        org.apache.log4j.PropertyConfigurator.configure(DefactoDemo.class.getClassLoader().getResource("properties/log4j.properties"));

        List<DefactoModel> models = new ArrayList<>();
        Defacto.init();
        models.add(getEinsteinModel());
        //models = getRDFModels();
        Defacto.checkFacts(models, TIME_DISTRIBUTION_ONLY.NO);


//        Defacto.checkFacts(new DefactoConfig(new Ini(new File("defacto.ini"))), getTrainingData());
    }

    public static DefactoModel getEinsteinModel() {
        final Model model = ModelFactory.createDefaultModel();
        model.read(DefactoModel.class.getClassLoader().getResourceAsStream("Einstein.ttl"), null,
                "TURTLE");
        return new DefactoModel(model, "Einstein Model", true, Arrays.asList("en"));
    }
    
    public static List<DefactoModel> getRDFModels() throws FileNotFoundException
    {
    	List<DefactoModel> models = new ArrayList<>();
    	File dir = new File("E:\\RDFModels");
    	  File[] directoryListing = dir.listFiles();
    	  if (directoryListing != null) {
    	    for (File child : directoryListing) {
    	      final Model model = ModelFactory.createDefaultModel();
    	      model.read(new FileInputStream(child), null, "TURTLE");
    	      models.add(new DefactoModel(model, "leader", false, Arrays.asList("en")));
    	    }
    	  } else {
    	    // Handle the case where dir is not really a directory.
    	    // Checking dir.isDirectory() above would not be sufficient
    	    // to avoid race conditions with another process that deletes
    	    // directories.
    	  }
    	return models;
    }
    
    public static List<DefactoModel> getTrainingData() {

        List<DefactoModel> models = new ArrayList<DefactoModel>();
        List<File> modelFiles = new ArrayList<File>(Arrays.asList(new File("resources/training/data/true").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/domain").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/range").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/domain_range").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/property").listFiles()));
//        modelFiles.addAll(Arrays.asList(new File("resources/training/data/false/random").listFiles()));
        Collections.sort(modelFiles);
//        Collections.shuffle(modelFiles);
        List<String> confirmedFilenames = null;
        try {
            
            confirmedFilenames = FileUtils.readLines(new File("resources/properties/confirmed_properties.txt"));
        }
        catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        
        for (File mappingFile : modelFiles) {
            
            // dont use svn files
            if ( !mappingFile.isHidden() && confirmedFilenames.contains(mappingFile.getName())) {//&&  models.size() < 100) {
                
                try {
                    
                    Model model = ModelFactory.createDefaultModel();
                    model.read(new FileReader(mappingFile), "", "TTL");
                    String name = mappingFile.getParent().replace("resources/training/data/", "") + "/" +mappingFile.getName();
                    boolean isCorrect = false;

                    if ( mappingFile.getAbsolutePath().contains("data/true") ) isCorrect = true;
                    logger.info("Loading "+isCorrect+" triple from file: " + mappingFile.getName());
                        
                    models.add(new DefactoModel(model, name, isCorrect, Arrays.asList("en")));
                }
                catch (FileNotFoundException e) {
                    
                    e.printStackTrace();
                }
            }
        }
        Collections.shuffle(models);
        return models;
    }

    /**
     * @return a set of two models which contain each a fact and the appropriate labels for the resources
     */
    private static List<DefactoModel> getSampleData(){
        
        Model model1 = ModelFactory.createDefaultModel();
        
        Resource albert = model1.createResource("http://dbpedia.org/resource/Albert_Einstein");
        albert.addProperty(RDFS.label, "Albert Einstein");
        Resource ulm = model1.createResource("http://dbpedia.org/resource/Ulm");
        ulm.addProperty(RDFS.label, "Ulm");
        albert.addProperty(model1.createProperty("http://dbpedia.org/ontology/birthPlace"), ulm);
        
        Model model2 = ModelFactory.createDefaultModel();
        
        Resource quentin = model2.createResource("http://dbpedia.org/resource/Quentin_Tarantino");
        quentin.addProperty(RDFS.label, "Quentin Tarantino");
        Resource deathProof = model2.createResource("http://dbpedia.org/resource/Death_Proof");
        deathProof.addProperty(RDFS.label, "Death Proof");
        deathProof.addProperty(model2.createProperty("http://dbpedia.org/ontology/director"), quentin);
        
        Model model3 = ModelFactory.createDefaultModel();
        
        Resource germany = model3.createResource("http://dbpedia.org/resource/Germany");
        germany.addProperty(RDFS.label, "Germany");
        Resource berlin = model3.createResource("http://dbpedia.org/resource/Bonn");
        berlin.addProperty(RDFS.label, "Bonn");
        berlin.addProperty(model3.createProperty("http://dbpedia.org/ontology/capital"), germany);
        
        Model model4 = ModelFactory.createDefaultModel();
        
        Resource ballack = model4.createResource("http://dbpedia.org/resource/Michael_Ballack");
        ballack.addProperty(RDFS.label, "Ballack");
        Resource chelsea = model4.createResource("http://dbpedia.org/resource/Chelsea_F.C.");
        chelsea.addProperty(RDFS.label, "Chelsea");
        chelsea.addProperty(model4.createProperty("http://dbpedia.org/ontology/team"), ballack);
        
        Model model5 = ModelFactory.createDefaultModel();
        
        Resource ronaldo = model5.createResource("http://dbpedia.org/resource/Cristiano_Ronaldo");
        ronaldo.addProperty(RDFS.label, "Cristiano Ronaldo");
        Resource manu = model5.createResource("http://dbpedia.org/resource/Manchester_United_F.C.");
        manu.addProperty(RDFS.label, "United");
        manu.addProperty(model5.createProperty("http://dbpedia.org/ontology/team"), ronaldo);
        
        List<DefactoModel> models = new ArrayList<DefactoModel>();
//        models.add(new DefactoModel(model1, "albert", true));
//        models.add(new DefactoModel(model2, "quentin", true));
//        models.add(new DefactoModel(model3, "bonn", true));
        models.add(new DefactoModel(model4, "ballack", true, Arrays.asList("en")));
//        models.add(new DefactoModel(model5, "ronaldo", true));
        
        return models;
    }
}
