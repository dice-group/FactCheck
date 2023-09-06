package org.aksw.defacto;


import java.io.*;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.config.DefactoConfig;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.ml.feature.evidence.AbstractEvidenceFeature;
import org.aksw.defacto.ml.feature.evidence.EvidenceFeatureExtractor;
import org.aksw.defacto.ml.feature.evidence.EvidenceScorer;
import org.aksw.defacto.ml.feature.fact.AbstractFactFeatures;
import org.aksw.defacto.ml.feature.fact.FactFeatureExtraction;
import org.aksw.defacto.ml.feature.fact.FactScorer;
import org.aksw.defacto.ml.feature.fact.impl.WordnetExpensionFeature;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.cache.solr.Solr4SearchResultCache;
import org.aksw.defacto.search.crawl.EvidenceCrawler;
import org.aksw.defacto.search.query.MetaQuery;
import org.aksw.defacto.search.query.QueryGenerator;
import org.aksw.defacto.util.BufferedFileWriter;
import org.aksw.defacto.util.BufferedFileWriter.WRITER_WRITE_MODE;
import org.aksw.defacto.util.Encoder.Encoding;
import org.aksw.defacto.util.FileReader;

import org.aksw.defacto.util.TimeUtil;

import org.apache.jena.base.Sys;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.dice.factcheck.search.engine.elastic.ElasticSearchEngine;
import org.ini4j.Ini;
import org.ini4j.InvalidFileFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class Defacto {

	public enum TIME_DISTRIBUTION_ONLY{

		YES,
		NO;
	}

	public static DefactoConfig DEFACTO_CONFIG;
	public static TIME_DISTRIBUTION_ONLY onlyTimes;
	private static final Logger LOGGER = LoggerFactory.getLogger(Defacto.class);

	/**
	 * @param model the model to check. this model may only contain the link between two resources
	 * which needs to be checked and the labels (Constants.RESOURCE_LABEL) for the resources which means it
	 * needs to contain only these three triples
	 * 
	 * @return
	 */
	public static Evidence checkFact(DefactoModel model, TIME_DISTRIBUTION_ONLY onlyTimes) {

		//FactClassifierModelTrainer factTrainer = new FactClassifierModelTrainer();
		//factTrainer.trainClassifier();

		//EvidenceClassifierModelTrainer evidenceTrainer = new EvidenceClassifierModelTrainer();
		//evidenceTrainer.trainClassifier();


		init();
		LOGGER.info("Checking fact: " + model);
		Defacto.onlyTimes = onlyTimes;

		// hack to get surface forms before timing
		// not needed anymore, since surfaceforms are inside model
		// SubjectObjectFactSearcher.getInstance();
		// not needed anymore since we do not use NER tagging
		// NlpModelManager.getInstance();

		// 1. generate the search engine queries
		long start = System.currentTimeMillis();
		QueryGenerator queryGenerator = new QueryGenerator(model);
		Map<Pattern,MetaQuery> queries = new HashMap<Pattern,MetaQuery>();
		for ( String language : model.languages ) 
			queries.putAll(queryGenerator.getSearchEngineQueries(language));

		if ( queries.size() <= 0 ) return new Evidence(model); 
		LOGGER.info("Preparing queries took " + TimeUtil.formatTime(System.currentTimeMillis() - start));
		
		// 2. download the search results in parallel
		long startCrawl = System.currentTimeMillis();
		EvidenceCrawler crawler = new EvidenceCrawler(model, queries);
		Evidence evidence = crawler.crawlEvidence();
		LOGGER.info("Crawling evidence took " + TimeUtil.formatTime(System.currentTimeMillis() - startCrawl));
		
		// short cut to avoid unnecessary computation
		if ( onlyTimes.equals(TIME_DISTRIBUTION_ONLY.YES) ) return evidence;

		// 3. confirm the facts
		long startFactConfirmation = System.currentTimeMillis();
		FactFeatureExtraction factFeatureExtraction = new FactFeatureExtraction();
		factFeatureExtraction.extractFeatureForFact(evidence);
		LOGGER.info("Fact feature extraction took " + TimeUtil.formatTime(System.currentTimeMillis() - startFactConfirmation));
		
		// 
		// 4. score the facts
		long startFactScoring = System.currentTimeMillis();
		FactScorer factScorer = new FactScorer();
		factScorer.scoreEvidence(evidence);
		LOGGER.info("Fact Scoring took " + TimeUtil.formatTime(System.currentTimeMillis() - startFactScoring));
		
		// 5. calculate the factFeatures for the model
		long startFeatureExtraction = System.currentTimeMillis();
		EvidenceFeatureExtractor featureCalculator = new EvidenceFeatureExtractor();
		featureCalculator.extractFeatureForEvidence(evidence);
		LOGGER.info("Evidence feature extraction took " + TimeUtil.formatTime(System.currentTimeMillis() - startFeatureExtraction));
		
		//if ( Defacto.DEFACTO_CONFIG.getBooleanSetting("settings", "TRAINING_MODE") ) {

			long startScoring = System.currentTimeMillis();
			EvidenceScorer scorer1 = new EvidenceScorer();
			scorer1.scoreEvidence(evidence);
			LOGGER.info("Evidence Scoring took " + TimeUtil.formatTime(System.currentTimeMillis() - startScoring));
			
		//}

		LOGGER.info("Overall time for fact: " +  TimeUtil.formatTime(System.currentTimeMillis() - start));
		
		return evidence;
	}

	public static void writeFactTrainingFiles(String filename) {

		// rewrite the fact training file after every proof
		if ( DEFACTO_CONFIG.getBooleanSetting("fact", "OVERWRITE_FACT_TRAINING_FILE") ) writeFactTrainingDataFile(filename);
	}

	public static void writeEvidenceTrainingFiles(String filename) {

		// rewrite the training file after every checked triple
		if ( DEFACTO_CONFIG.getBooleanSetting("evidence", "OVERWRITE_EVIDENCE_TRAINING_FILE")  ) writeEvidenceTrainingDataFile(filename);
	}

	public static void init(){

		try {

			if ( Defacto.DEFACTO_CONFIG  == null ) {
				LOGGER.info("read defacto config from class org.aksw.defacto.Defacto: ");
				File targetFile = FileReader.read("org.aksw.defacto.Defacto","defacto.ini");
				LOGGER.info(" ini file path is "+ targetFile.getAbsolutePath());
				LOGGER.info("read defacto config from : ");
				Defacto.DEFACTO_CONFIG = new DefactoConfig(new Ini(targetFile));

			}
		} catch (InvalidFileFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

		ElasticSearchEngine.init();
		Solr4SearchResultCache.inti();
		BoaPatternSearcher.init();
		WordnetExpensionFeature.init();
	}

	/**
	 * 
	 * @param defactoModel
	 * @return
	 * @throws IOException 
	 */
	public static Map<DefactoModel,Evidence> checkFacts(List<DefactoModel> defactoModel, TIME_DISTRIBUTION_ONLY onlyTimeDistribution) throws IOException {

		init();
		Model m = ModelFactory.createDefaultModel();
		Map<DefactoModel,Evidence> evidences = new HashMap<DefactoModel, Evidence>();
		int i=0;
		for (DefactoModel model : defactoModel) {

			Evidence evidence = checkFact(model, onlyTimeDistribution);
			evidences.put(model, evidence);

			// we want to print the score of the classifier 
			if ( !Defacto.DEFACTO_CONFIG.getBooleanSetting("settings", "TRAINING_MODE") ) 
			{
				System.out.println("Defacto: " + new DecimalFormat("0.00").format(evidence.getDeFactoScore()) + " % that this fact is true!");
				BufferedFileWriter writer = new BufferedFileWriter("F:\\Test4.csv", Encoding.UTF_8, WRITER_WRITE_MODE.APPEND);
				PrintWriter out = new PrintWriter(writer);
				//out.println(AbstractEvidenceFeature.provenance.toString());
				out.println(model.name+","+new DecimalFormat("0.00").format(evidence.getDeFactoScore()));
				writer.close();
			}
			// rewrite the fact training file after every proof
			if ( DEFACTO_CONFIG.getBooleanSetting("fact", "OVERWRITE_FACT_TRAINING_FILE") ) 
				writeFactTrainingDataFile(DEFACTO_CONFIG.getStringSetting("fact", "FACT_TRAINING_DATA_FILENAME"));

			// rewrite the training file after every checked triple
			if ( DEFACTO_CONFIG.getBooleanSetting("evidence", "OVERWRITE_EVIDENCE_TRAINING_FILE")  ) 
				writeEvidenceTrainingDataFile(DEFACTO_CONFIG.getStringSetting("evidence", "EVIDENCE_TRAINING_DATA_FILENAME"));
			//Defacto.wirteModel(m, model.model, "award_00001.ttl", "http://dbpedia.org/ontology/recievedAward", "http://dbpedia.org/ontology/award", (float)0.0, true);
		}        
		return evidences;
	}

	/**
	 * 
	 */
	private static void writeEvidenceTrainingDataFile(String filename) {

		BufferedFileWriter writer = new BufferedFileWriter(DefactoConfig.DEFACTO_DATA_DIR + filename, Encoding.UTF_8, WRITER_WRITE_MODE.OVERRIDE);
		PrintWriter out = new PrintWriter(writer);
		out.println(AbstractEvidenceFeature.provenance.toString());
		out.println();
		writer.close();
	}

	/**
	 * this tries to write an arff file which is also compatible with google docs spreadsheets
	 */
	private static void writeFactTrainingDataFile(String filename) {

		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(DefactoConfig.DEFACTO_DATA_DIR + filename, false));
			PrintWriter out = new PrintWriter(writer);
			out.println(AbstractFactFeatures.factFeatures.toString());
			writer.close();
		}
		catch (IOException e) {

			e.printStackTrace();
		}        
	}

	public void main(String[] args) {

		int max = 0;
		int min = 1000;

		for ( int i = 0; i < 1000 ; i++) {

			int j = new Integer(0 + (int)((9 - 0 + 1) * Math.random()));

			max = Math.max(max, j);
			min = Math.min(min, j);
		}

		LOGGER.info("MAX: " + max);
		LOGGER.info("MIN: " + min);
	}
}
