/**
 * 
 */
package org.aksw.defacto.evaluation;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

import org.aksw.defacto.Defacto;
import org.aksw.defacto.Defacto.TIME_DISTRIBUTION_ONLY;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.evidence.DummyEvidenceScorer;
import org.aksw.defacto.ml.feature.evidence.EvidenceScorer;
import org.aksw.defacto.ml.feature.evidence.Scorer;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.reader.DefactoModelReader;
import org.aksw.defacto.util.Frequency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import weka.classifiers.Classifier;
import weka.core.Instance;
import weka.core.Instances;

/**
 * @author Daniel Gerber <daniel.gerber@deinestadtsuchtdich.de>
 *
 */
public class EvidenceEvaluation {
	private static final Logger LOGGER = LoggerFactory.getLogger(EvidenceEvaluation.class);
	private static Classifier classifier = null; 
	private static Instances trainingInstances = null;
	private static Instances backupInstances = null;
	private static Map<Integer,Integer> correctYearMap = new TreeMap<>();
	private static Map<Integer,Integer> wrongYearMap = new TreeMap<>();
	
public void main(String[] args) throws Exception {
		
		Defacto.init();
		loadClassifier();
	    //loadClassifier(predicate);
		initYearMap();
//		testFactArff();
		
		Map<String,Integer> relationToCorrectCount = new TreeMap<>();
		trainingInstances = new Instances(new BufferedReader(new FileReader(DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "ARFF_EVIDENCE_TRAINING_DATA_FILENAME"))));
		backupInstances = new Instances(new BufferedReader(new FileReader(DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "ARFF_EVIDENCE_TRAINING_DATA_FILENAME"))));
		trainingInstances.setClassIndex(16);
		trainingInstances.deleteAttributeAt(0);
		
		System.out.println(String.format("Got %s instances!", 	trainingInstances.numInstances()));
		System.out.println(String.format("Got %s attributes!", 	trainingInstances.numAttributes()));
		System.out.println(String.format("Got %s classes!", 	trainingInstances.numClasses()));
		System.out.println(String.format("Got %s instances!", 	backupInstances.numInstances()));
		System.out.println(String.format("Got %s attributes!", 	backupInstances.numAttributes()));
//		System.out.println(String.format("Got %s classes!", 	backupInstances.numClasses()));
		
		int sum = 0;
		
		Frequency freq = new Frequency();
		
		for ( int i = 0 ; i < backupInstances.numInstances() -1; i++) {
			Instance instance = trainingInstances.instance(i);
			String relation = backupInstances.instance(i).attribute(0).value(i);
			relation = relation.replaceAll("(train|test)/correct/", "").replaceAll("/.*", "");
//			
			
			freq.addValue(classifier.distributionForInstance(instance)[0]);
			
			DefactoModel model = DefactoModelReader.readModel("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/FactBench/v1/" + backupInstances.instance(i).attribute(0).value(i));
			String averageYear = ((model.timePeriod.to + model.timePeriod.from) / 2) + "";
			averageYear = averageYear.substring(0, 3) + "0";
			if ( Integer.valueOf(averageYear) < 1900 ) averageYear = "1890";
			
			if ( classifier.distributionForInstance(instance)[0] >= 0.5D ) {
				
				sum++;
				if ( !relationToCorrectCount.containsKey(relation) ) relationToCorrectCount.put(relation, 1);
				else relationToCorrectCount.put(relation, relationToCorrectCount.get(relation) + 1);
				
				correctYearMap.put(Integer.valueOf(averageYear), correctYearMap.get(Integer.valueOf(averageYear)) + 1);
			}
			else {
				
				wrongYearMap.put(Integer.valueOf(averageYear), wrongYearMap.get(Integer.valueOf(averageYear)) + 1);
				System.out.println(model);
//				System.out.println(backupInstances.instance(i).attribute(0).value(i));
			}
		}
		int count = 0;
		
		for ( int j = 1890; j <= 2010 ; j = j+10 ) {

			Integer correct = correctYearMap.get(j);
			Integer wrong = wrongYearMap.get(j);
			Double ratio = (((wrong * 100) / (double)(wrong + correct)) * (wrong + correct)) / 3700 ;
			ratio = correct / ((double) correct + wrong);
			String year = j + "";
			if ( j == 1890 ) year = "< 1890";
			
			count += correct;
			count += wrong;
			
			System.out.println(year + "\t&\t" + correct + "\t&\t" + wrong + "\t&\t" + String.format(Locale.ENGLISH, "%.3f", ratio)+ "\\\\");
		}
		
		System.out.println("Count: " + count);
		
//		for ( Entry<Comparable<?>, Long> sortByValue : freq.sortByValue() ) {
//			
//			System.out.println(sortByValue.getKey() + "\t" +sortByValue.getValue());
//		}
		
//		System.out.println("All: " + sum);
		for (Map.Entry<String, Integer> entry : relationToCorrectCount.entrySet() ){
			
			System.out.println(entry.getKey() + "\t" + entry.getValue());
		}
	}
	
//	public void main(String[] args) throws Exception {
//		
//		Defacto.init();
//		loadClassifier();
//		initYearMap();
//		testFactArff();
//		
//		Map<String,Integer> relationToCorrectCount = new TreeMap<>();
//		trainingInstances = new Instances(new BufferedReader(new FileReader(DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "ARFF_EVIDENCE_TRAINING_DATA_FILENAME"))));
//		backupInstances = new Instances(new BufferedReader(new FileReader(DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "ARFF_EVIDENCE_TRAINING_DATA_FILENAME"))));
//		trainingInstances.setClassIndex(16);
//		trainingInstances.deleteAttributeAt(0);
//		
//		System.out.println(String.format("Got %s instances!", 	trainingInstances.numInstances()));
//		System.out.println(String.format("Got %s attributes!", 	trainingInstances.numAttributes()));
//		System.out.println(String.format("Got %s classes!", 	trainingInstances.numClasses()));
//		System.out.println(String.format("Got %s instances!", 	backupInstances.numInstances()));
//		System.out.println(String.format("Got %s attributes!", 	backupInstances.numAttributes()));
////		System.out.println(String.format("Got %s classes!", 	backupInstances.numClasses()));
//		
//		int sum = 0;
//		
//		Frequency freq = new Frequency();
//		
//		for ( int i = 0 ; i < backupInstances.numInstances() -1; i++) {
//			Instance instance = trainingInstances.instance(i);
//			String relation = backupInstances.instance(i).attribute(0).value(i);
//			relation = relation.replaceAll("(train|test)/correct/", "").replaceAll("/.*", "");
////			
//			
//			freq.addValue(classifier.distributionForInstance(instance)[0]);
//			
//			DefactoModel model = DefactoModelReader.readModel("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/FactBench/v1/" + backupInstances.instance(i).attribute(0).value(i));
//			String averageYear = ((model.timePeriod.to + model.timePeriod.from) / 2) + "";
//			averageYear = averageYear.substring(0, 3) + "0";
//			if ( Integer.valueOf(averageYear) < 1900 ) averageYear = "1890";
//			
//			if ( classifier.distributionForInstance(instance)[0] < 0.5D ) {
//				
//				sum++;
//				if ( !relationToCorrectCount.containsKey(relation) ) relationToCorrectCount.put(relation, 1);
//				else relationToCorrectCount.put(relation, relationToCorrectCount.get(relation) + 1);
//				
//				correctYearMap.put(Integer.valueOf(averageYear), correctYearMap.get(Integer.valueOf(averageYear)) + 1);
//			}
//			else {
//				
//				wrongYearMap.put(Integer.valueOf(averageYear), wrongYearMap.get(Integer.valueOf(averageYear)) + 1);
////				System.out.println(model);
////				System.out.println(backupInstances.instance(i).attribute(0).value(i));
//			}
//		}
//		
//		for ( int j = 1890; j <= 2010 ; j = j+10 ) {
//
//			Integer correct = correctYearMap.get(j);
//			Integer wrong = wrongYearMap.get(j);
//			Double ratio = (((wrong * 100) / (double)(wrong + correct)) * (wrong + correct)) / 3700 ;
//			String year = j + "";
//			if ( j == 1890 ) year = "< 1890";
//			
//			System.out.println(year + "\t&\t" + correct + "\t&\t" + wrong + "\t&\t" + String.format(Locale.ENGLISH, "%.1f", ratio)+ "\\\\");
//		}
//		
////		for ( Entry<Comparable<?>, Long> sortByValue : freq.sortByValue() ) {
////			
////			System.out.println(sortByValue.getKey() + "\t" +sortByValue.getValue());
////		}
//		
////		System.out.println("All: " + sum);
//		for (Map.Entry<String, Integer> entry : relationToCorrectCount.entrySet() ){
//			
//			System.out.println(entry.getKey() + "\t" + entry.getValue());
//		}
//	}

	private static void testFactArff() throws FileNotFoundException, IOException {
		
		try {
			Instances instances = new Instances(new BufferedReader(new FileReader("/Users/gerb/Development/workspaces/experimental/defacto/mltemp/machinelearning/model/fact/66_33_proof_smo_reg/66_33_proof_smo_reg_polykernel.arff")));
			
			Frequency freq = new Frequency();
			
			for ( int i = 0 ; i < instances.numInstances(); i++) {
				
				freq.addValue(instances.instance(i).value(0));// + " " +  instances.instance(i).stringValue(26));
			}
			
			for ( Entry<Comparable<?>, Long> sortByValue : freq.sortByValue()){
				
				System.out.println(sortByValue.getKey() + ": " + sortByValue.getValue());
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
	}

	private static void initYearMap() {
		
		for ( int i = 1890; i <= 2010 ; i = i+10 ) {
			
			correctYearMap.put(i, 0);
			wrongYearMap.put(i, 0);
		}
	}

	private static void loadClassifier() {

        try {
            
            classifier = (Classifier) weka.core.SerializationHelper.read(
            		DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE"));
        }
        catch (Exception e) {

            throw new RuntimeException("Could not loa7d classifier from: " +
            		DefactoConfig.DEFACTO_DATA_DIR + Defacto.DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_CLASSIFIER_TYPE"), e);
        }
    }

	private Classifier loadClassifier(String predicate) {

		try {
			String modelToUse = whichModelShouldUse(predicate);
			LOGGER.info(" Evidence Evaluation use this model :  "+modelToUse);
			return (Classifier) weka.core.SerializationHelper.read(
					DefactoConfig.DEFACTO_DATA_DIR + modelToUse);
		}
		catch (Exception e) {

			throw new RuntimeException("Could not load classifier from: " +
					DefactoConfig.DEFACTO_DATA_DIR + whichModelShouldUse(predicate));
		}
	}

	private String whichModelShouldUse(String predicate) {
		List<String> predicates =  Arrays.asList(Defacto.DEFACTO_CONFIG.getArray("predicates","predicate"));
		String[] modelIndex = Defacto.DEFACTO_CONFIG.getArray("predicates","EVIDENCE_CLASSIFIER_TYPE_model_index");
		String[] factClassifiers = Defacto.DEFACTO_CONFIG.getArray("predicates","EVIDENCE_CLASSIFIER_TYPE");

		if(predicates.contains(predicate)){
			int index = predicates.indexOf(predicate);
			return factClassifiers[Integer.parseInt(modelIndex[index])];
		}else{
			if(factClassifiers.length > 0 && modelIndex.length>0) {
				return factClassifiers[Integer.parseInt(modelIndex[0])];
			}else{
				return "ERROR";
			}
		}
	}
}
