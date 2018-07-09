package org.dice.factcheck.proof.extract;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.aksw.defacto.search.fact.FactSearcher;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.stanford.nlp.coref.CorefCoreAnnotations;
import edu.stanford.nlp.coref.CorefCoreAnnotations.CorefChainAnnotation;
import edu.stanford.nlp.coref.data.CorefChain;
import edu.stanford.nlp.coref.data.CorefChain.CorefMention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;


public class SubjectObjectProofExtractor implements FactSearcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubjectObjectProofExtractor.class);
	private static final Set<String> stopwords = new HashSet<String>(Arrays.asList("the", "of", "and"));

	private static final java.util.regex.Pattern ROUND_BRACKETS     = java.util.regex.Pattern.compile("\\(.+?\\)");
	private static final java.util.regex.Pattern SQUARED_BRACKETS   = java.util.regex.Pattern.compile("\\[.+?\\]");
	private static final java.util.regex.Pattern TRASH              = java.util.regex.Pattern.compile("[^\\p{L}\\p{N}.?!' ]");
	private static final java.util.regex.Pattern WHITESPACES        = java.util.regex.Pattern.compile("\\n");

	/**
	 * 
	 */
	public SubjectObjectProofExtractor() {
	}


	/**
	 *  Extract proof phrases from a website required to confirm fact
	 */

	@Override
	public void generateProofs(Evidence evidence, WebSite website, DefactoModel model, Pattern pattern) {

		try
		{
			Set<String> subjectLabels = new HashSet<String>();
			Set<String> objectLabels = new HashSet<String>();

			/****** Get the surface forms for Subject and Object *********/

			for ( String language : model.getLanguages() ) {
				subjectLabels.add(model.getSubjectLabelNoFallBack(language));
				subjectLabels.addAll(model.getSubjectAltLabels(language));

				objectLabels.add(model.getObjectLabelNoFallBack(language));
				objectLabels.addAll(model.getObjectAltLabels(language));
			}
			subjectLabels.remove(Constants.NO_LABEL);
			objectLabels.remove(Constants.NO_LABEL);
			String subjectLabel = evidence.getModel().getSubjectLabel(null);

			for (String label : subjectLabel.split(" ")) {
				if(label.length()>2)
					subjectLabels.add(label.trim());
			}	

			/**** Create regex Patterns for Subject and Object forms and add to list ****/

			List<java.util.regex.Pattern> subpatterns = new ArrayList<java.util.regex.Pattern>();
			Iterator<String> it = subjectLabels.iterator();
			while(it.hasNext())
				subpatterns.add(java.util.regex.Pattern.compile(it.next().toString(), java.util.regex.Pattern.CASE_INSENSITIVE));

			List<java.util.regex.Pattern> objpatterns = new ArrayList<java.util.regex.Pattern>();
			Iterator<String> itob = objectLabels.iterator();
			while(itob.hasNext())
				objpatterns.add(java.util.regex.Pattern.compile(itob.next().toString(), java.util.regex.Pattern.CASE_INSENSITIVE));

			/**** Normalize website text using pattern replacement and store it in String ****/
			// replace all the surface forms identified with normalized string

			String normalizedText = website.getText();
			for (java.util.regex.Pattern pattern2 : subpatterns) {
				normalizedText = pattern2.matcher(normalizedText).replaceAll("subjectFound");
			}
			for (java.util.regex.Pattern pattern2 : objpatterns) {
				normalizedText = pattern2.matcher(normalizedText).replaceAll("objectFound");
			}

			/**** Annotate the website text and normalized website text using SNLP sentence split "ssplit" ****/

			Annotation docNormalized = model.corenlpClient.sentenceAnnotation(normalizedText);
			Annotation docOriginal = model.corenlpClient.sentenceAnnotation(website.getText());

			/**** Find proof phrases in both direction i.e., subject followed by object and vice-versa ****/

			HashMap<String, Integer> subOjectPhrases = findProofPhrase(docNormalized, docOriginal, "subjectFound", "objectFound");
			HashMap<String, Integer> objSubjectPhrases = findProofPhrase(docNormalized, docOriginal, "objectFound", "subjectFound");
			subOjectPhrases.putAll(objSubjectPhrases);

			Set<String> surfaceForms = new HashSet<String>(subjectLabels);
			surfaceForms.addAll(objectLabels);

			/**** Now create the proofs ****/
			createProofsForEvidence(evidence, subOjectPhrases, website, surfaceForms, subjectLabels, objectLabels);
		}
		catch (Exception e) {
			LOGGER.info("Exception while extracting proof phrases from web page: "+e.getMessage());
		}

	}

	public static void toLowerCase(Set<String> strings)
	{
		String[] stringsArray = strings.toArray(new String[0]);
		for (int i=0; i< stringsArray.length; ++i) {
			stringsArray[i] = stringsArray[i].toLowerCase();
		}
		strings.clear();
		strings.addAll(Arrays.asList(stringsArray));
	}

	public static void toUpperCase(Set<String> strings)
	{
		String[] stringsArray = strings.toArray(new String[0]);
		for (int i=0; i< stringsArray.length; ++i) {
			stringsArray[i] = stringsArray[i].toUpperCase();
		}
		strings.clear();
		strings.addAll(Arrays.asList(stringsArray));
	}

	// recursively checks to find  shortest string occurrence having subject and object label
	private String breakString(String input, String leftLabel, String rightLabel)
	{
		String temp="";
		String tempInput=input;
		do
		{
			temp = tempInput;
			tempInput = org.apache.commons.lang3.StringUtils.substringBetween(temp+rightLabel, leftLabel, rightLabel);
		}while(tempInput!=null);
		return temp;
	}

	/**
	 *  Creates evidence from proof phrases
	 */

	private void createProofsForEvidence(Evidence evidence, HashMap<String, Integer> matches, WebSite site, Set<String> surfaceForms, Set<String> subjectlabels, Set<String> objectlabels) {

		for(Map.Entry<String, Integer> entry : matches.entrySet())
		{
			String tinyContext = "";
			String sublabel ="";
			String objlabel ="";
			String proofPhrase = entry.getKey();
			// it makes no sense to look at longer strings 
			if ( proofPhrase.split(" ").length < Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_OF_TOKENS_BETWEEN_ENTITIES") ) {

				try
				{						
					String resolvedStr = applyCorefResolution(proofPhrase, evidence.getModel());

					// If the proof contain multiple sentences shorten after performing corefernce on
					// it because small proofs are easier to confirm 					
					if(entry.getValue()>1)
					{
						Annotation annotatedDoc = evidence.getModel().corenlpClient.sentenceAnnotation(resolvedStr);

						List<CoreMap> sentences = annotatedDoc.get(CoreAnnotations.SentencesAnnotation.class);
						for (CoreMap sentence : sentences) {
							String sentenceString = sentence.get(CoreAnnotations.TextAnnotation.class).toLowerCase();
							boolean subfound = false;
							boolean objfound = false;
							for (String string : subjectlabels) {
								if(StringUtils.containsIgnoreCase(sentenceString, string))
								{
									subfound = true;
									sublabel = string;
									//break;
								}
							}
							for (String string : objectlabels) {
								if(StringUtils.containsIgnoreCase(sentenceString, string))
								{
									objfound = true;
									objlabel = string;
									//break;
								}
							}

							if(subfound && objfound)
							{
								proofPhrase = sentence.get(CoreAnnotations.TextAnnotation.class);
								if(!(StringUtils.substringBetween(proofPhrase.toLowerCase(), sublabel.toLowerCase(), objlabel.toLowerCase())==null))
									tinyContext = breakString(proofPhrase.toLowerCase(), sublabel.toLowerCase(), objlabel.toLowerCase());
								else
									tinyContext = breakString(proofPhrase.toLowerCase(), objlabel.toLowerCase(), sublabel.toLowerCase());
								break;
							}

							else if ((subfound || objfound) && sentence.toString().contains(site.getPredicate()))
							{
								tinyContext = sentence.get(CoreAnnotations.TextAnnotation.class);
								proofPhrase = resolvedStr;
								break;
							}

						}
					}
					else
					{
						proofPhrase = resolvedStr;
						for (String string : subjectlabels) {
							if(StringUtils.containsIgnoreCase(proofPhrase, string))
							{
								sublabel = string;
								//break;
							}
						}
						for (String string : objectlabels) {
							if(StringUtils.containsIgnoreCase(proofPhrase, string))
							{
								objlabel = string;
								//break;
							}
						}
						if(!(StringUtils.substringBetween(proofPhrase.toLowerCase(), sublabel.toLowerCase(), objlabel.toLowerCase())==null))
							tinyContext = breakString(proofPhrase.toLowerCase(), sublabel.toLowerCase(), objlabel.toLowerCase());
						else
							tinyContext = breakString(proofPhrase.toLowerCase(), objlabel.toLowerCase(), sublabel.toLowerCase());
					}

					if(sublabel.isEmpty()) sublabel = evidence.getModel().getSubjectLabel("en");
					if(objlabel.isEmpty()) objlabel = evidence.getModel().getObjectLabel("en");
					ComplexProof proof = new ComplexProof(evidence.getModel(), sublabel, objlabel, proofPhrase.trim(), normalizeOccurrence(proofPhrase.trim(),surfaceForms), site);
					//proof.setTinyContext(tinyContext.trim());

					evidence.addComplexProof(proof);
				}
				catch (NullPointerException e)
				{
					LOGGER.info("Caught Exception while creating Proof Evidence: "+e.getMessage());
				}
			}
		}
	}


	/**
	 *  Perform a coreference resolution to identify mention of same entities
	 *  in a sentence or sequence of sentences.
	 */

	private String applyCorefResolution(String sentencesString, DefactoModel model)
	{
		Annotation doc = model.corenlpClient.corefAnnotation(sentencesString);
		Map<Integer, CorefChain> corefs = doc.get(CorefChainAnnotation.class);
		List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);

		List<String> resolved = new ArrayList<String>();

		for (CoreMap sentence : sentences) {
			//System.out.println(sentence);

			List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

			for (CoreLabel token : tokens) {

				Integer corefClustId= token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
				CorefChain chain = null;
				if(corefs!=null)
					chain = corefs.get(corefClustId);

				if(chain==null){
					resolved.add(token.word());
				}else{

					int sentINdx = chain.getRepresentativeMention().sentNum -1;
					CoreMap corefSentence = sentences.get(sentINdx);
					List<CoreLabel> corefSentenceTokens = corefSentence.get(TokensAnnotation.class);
					String newwords = "";
					CorefMention reprMent = chain.getRepresentativeMention();
					if (token.index() <= reprMent.startIndex || token.index() >= reprMent.endIndex) {

						for (int i = reprMent.startIndex; i < reprMent.endIndex; i++) {
							CoreLabel matchedLabel = corefSentenceTokens.get(i - 1); 
							resolved.add(matchedLabel.word().replace("'s", ""));
							newwords += matchedLabel.word() + " ";
						}
					}

					else {
						resolved.add(token.word());
					}
				}
			}
		}
		String resolvedStr ="";
		//System.out.println();
		for (String str : resolved) {
			resolvedStr+=str+" ";
			//System.out.println(str);
		}

		return resolvedStr;

	}

	/**
	 * this method removes all thrash from the found pattern
	 *  - everything between "(" and ")"
	 *  - everything which is not a character or a number
	 *  - leading and trailing white-spaces
	 * 
	 * @param occurrence
	 * @param surfaceForms
	 * @return0000
	 */
	private String normalizeOccurrence(String occurrence, Set<String> surfaceForms) {

		// hopefully gain some performance improvements through using compiled patterns
		String normalizedOccurrence = ROUND_BRACKETS.matcher(occurrence).replaceAll("");
		normalizedOccurrence = SQUARED_BRACKETS.matcher(normalizedOccurrence).replaceAll("");
		normalizedOccurrence = TRASH.matcher(normalizedOccurrence).replaceAll(" ").trim();
		normalizedOccurrence = WHITESPACES.matcher(normalizedOccurrence).replaceAll(" ");

		//        String normalizedOccurrence = occurrence.replaceAll("\\(.+?\\)", "").replaceAll("\\[.+?\\]", "").replaceAll("[^\\p{L}\\p{N}.?!' ]", " ").trim().replaceAll("\\n\\r", " ");

		for ( String label : surfaceForms ) { label = label.toLowerCase();
		for (String part : label.split(" ") ) {

			if ( !stopwords.contains(part) ) {

				if (normalizedOccurrence.startsWith(part)) {

					normalizedOccurrence = StringUtils.replaceOnce(part, normalizedOccurrence, "");
					LOGGER.debug("Removed: ^" + part);
				}
				if (normalizedOccurrence.endsWith(part)) {

					normalizedOccurrence = normalizedOccurrence.replaceAll(part + "$", "");
					LOGGER.debug("Removed: " + part + "$");
				}
			}
		}
		}

		return normalizedOccurrence.trim();
	}


	/**
	 * Takes annotated documents as input and return the proof phrase containing proof string and 
	 * length in number of sentences
	 */

	private HashMap<String, Integer> findProofPhrase(Annotation docNormalized, Annotation docOriginal, String labelFirst, String labelSecond)
	{
		int sentenceCount = 0;
		int count = 0;
		int subjoccur = 0;
		boolean subjectFound = false;
		TreeMap<Integer, Integer> sent = new TreeMap<>();
		List<CoreMap> sentencesNormalized = docNormalized.get(CoreAnnotations.SentencesAnnotation.class);
		List<CoreMap> sentencesOriginal = docOriginal.get(CoreAnnotations.SentencesAnnotation.class);

		// process sentences to check if a sentence or sequence of sentences contain subject and object labels

		for (CoreMap sentence : sentencesNormalized) {

			String sentenceString = sentence.toString();

			// both the labels are found in a single sentence			
			if(sentenceString.contains(labelFirst) && sentenceString.contains(labelSecond))
			{
				subjectFound = false;
				sent.put(count, count);
				count++;
				continue;
			}

			// When only one label is found, mark it as start and look for other label
			if(sentenceString.contains(labelFirst))
			{
				subjectFound = true;
				sentenceCount = 1;
				subjoccur = count;
				count++;
				continue;
			}

			// When one label was already found in previous sentence and second label is not found
			// simply keep track and continue
			if(subjectFound && !(sentenceString.contains(labelSecond)))
			{
				sentenceCount++;
				count++;
				continue;
			}

			// When both labels are found in a sequence 
			// Note that, we currently limit sequence upto 4 sentences
			if(sentenceString.contains(labelSecond) && subjectFound)
			{
				if(sentenceCount<4)
				{
					sent.put(subjoccur, count);
				}
				else
				{
					subjoccur = 0;
				}
				subjectFound = false;
				count++;
				continue;
			}
			count++;
		}

		HashMap<String, Integer> subjectObjectStrOriginal = new HashMap<String, Integer>();

		// we need original sentences, we know the list of sentences with start and end indexes
		subjectObjectStrOriginal = getOriginalSentences(sentencesOriginal, sent);

		return subjectObjectStrOriginal;
	}


	/**
	 * Returns the original proof sentences from website text
	 */

	private HashMap<String, Integer> getOriginalSentences(List<CoreMap> sentencesNormal, TreeMap<Integer, Integer> sent)
	{
		HashMap<String, Integer> subjectObjectStrNormal = new HashMap<String, Integer>();
		for(Entry<Integer, Integer> entry : sent.entrySet()) {
			if(entry.getKey().equals(entry.getValue()))
			{
				if(entry.getKey()<=sentencesNormal.size()-1)
				{
					CoreMap senten = sentencesNormal.get(entry.getKey());
					subjectObjectStrNormal.put(senten.get(CoreAnnotations.TextAnnotation.class),1);
					continue;
				}
			}
			int k=0;
			String temp = "";
			for(int i = entry.getKey(); i<=entry.getValue();i++)
			{	    		  
				if(i<=sentencesNormal.size()-1)
				{
					if(!temp.isEmpty())
						temp = temp+" "+sentencesNormal.get(i).get(CoreAnnotations.TextAnnotation.class);
					else
						temp = temp+sentencesNormal.get(i).get(CoreAnnotations.TextAnnotation.class);
					k++;
				}
			}
			subjectObjectStrNormal.put(temp.trim(), k);
		}
		return subjectObjectStrNormal;
	}
}
