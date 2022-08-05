/**
 * 
 */
package org.aksw.defacto.ml.feature.fact;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;

import org.apache.xpath.operations.Bool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;


/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class FactScorer {

    private Classifier classifier       = null;
    private Instances trainingInstances = null;
    private static final Logger LOGGER = LoggerFactory.getLogger(FactScorer.class);
    /**
     * 
     */
    public FactScorer() {

        this.classifier = loadClassifier();
        try {
            
//            this.trainingInstances = new Instances(new BufferedReader(new FileReader(
//            		loadFileName("/training/arff/fact/defacto_fact_word.arff"))));
        	this.trainingInstances = new Instances(new BufferedReader(new FileReader(
        			DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("fact", "ARFF_TRAINING_DATA_FILENAME"))));
        }
        catch (FileNotFoundException e) {

            throw new RuntimeException(e);
        }
        catch (IOException e) {
            
            throw new RuntimeException(e);
        }
    }
    
    /**
     * 
     * @param evidence
     */
    public void scoreEvidence(Evidence evidence) {
        LOGGER.info("evidence is :"+evidence.toString());
        //Boolean isGeneratingTrainFile = false;
    	//Instances instancesWithStringVector = new Instances(trainingInstances);
        //instancesWithStringVector.setClassIndex(26);
        String fileName ="/home/farshad/experiments/Trainfactchek/Fact/"+evidence.getModel().getPredicate().getLocalName()+"_train1.arff";
        Instances withoutName = new Instances(AbstractFactFeatures.factFeatures);
        try(FileWriter fw = new FileWriter(fileName, false);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter out = new PrintWriter(bw))
        {
            out.println(withoutName.toString());
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        for ( ComplexProof proof : evidence.getComplexProofs() ) {
            LOGGER.info("proof is :"+proof.toString());
            try {
                withoutName = new Instances(AbstractFactFeatures.factFeatures);
                // create new instance and delete debugging features
                Instance newInstance = new Instance(proof.getFeatures());

                newInstance.deleteAttributeAt(28);
                newInstance.deleteAttributeAt(28);
                newInstance.deleteAttributeAt(28);
                newInstance.deleteAttributeAt(28);
                newInstance.deleteAttributeAt(28);

                withoutName.setClassIndex(withoutName.numAttributes() - 1);
                withoutName.deleteStringAttributes();

                newInstance.setDataset(withoutName);

                // insert all the words which occur
                /*for ( int i = 26 + 1 ; i < instancesWithStringVector.numAttributes(); i++) {
                    
                	List<String> parts = Arrays.asList(proof.getTinyContext().split(" "));
                    newInstance.insertAttributeAt(i);
                    Attribute attribute = instancesWithStringVector.attribute(i);
                    newInstance.setValue(attribute, parts.contains(attribute.name()) ? 1D : 0D);
                }
                newInstance.setDataset(instancesWithStringVector);
                instancesWithStringVector.add(newInstance);*/
                //System.out.println(this.classifier.distributionForInstance(newInstance)[0]);
                proof.setScore(this.classifier.distributionForInstance(newInstance)[0]);
                LOGGER.info("Proof score for " + " -> " + proof.getProofPhrase() +" -> "+proof.getScore());
                
                // remove the new instance again
                //instancesWithStringVector.delete();
            }
            catch (Exception e) {

                e.printStackTrace();
                System.exit(0);
            }
        }
        
        // set for each website the score by multiplying the proofs found on this site
        LOGGER.info("we have "+evidence.getAllWebSites().size()+" websites");
        for ( WebSite website : evidence.getAllWebSites() ) {
            
            double score = 1D;
            
            for ( ComplexProof proof : evidence.getComplexProofs(website)) {

                LOGGER.info("score is  "+score+" proofScore is "+ proof.getScore());

                score *= ( 1D - proof.getScore() );

                LOGGER.info(" new score is :"+ score);
            }
            website.setScore(1 - score);
        }
    }
    
    /**
     * 
     * @return
     */
    private Classifier loadClassifier() {

        try {
            
            return (Classifier) weka.core.SerializationHelper.read(
            		DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("fact", "FACT_CLASSIFIER_TYPE"));
        }
        catch (Exception e) {

            throw new RuntimeException("Could not load classifier from: " + 
            		DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("fact", "FACT_CLASSIFIER_TYPE"), e);
        }
    }
    
    public String loadFileName(String name){
    	
    	return new File(FactScorer.class.getResource(name).getFile()).getAbsolutePath(); 
    }
    
    public void main(String[] args) {
		
    	System.out.println("-"+ "".split(";").length + "-");
	}
}
