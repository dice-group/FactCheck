package org.aksw.defacto.search.fact;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aksw.defacto.Constants;
import org.aksw.defacto.Defacto;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.evidence.ComplexProof;
import org.aksw.defacto.evidence.Evidence;
import org.aksw.defacto.evidence.WebSite;
import org.aksw.defacto.model.DefactoModel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 * @author Jens Lehmann
 */
public class SubjectObjectFactSearcher implements FactSearcher {

	private static final Logger LOGGER = LoggerFactory.getLogger(SubjectObjectFactSearcher.class);
    private static final Set<String> stopwords = new HashSet<String>(Arrays.asList("the", "of", "and"));
    
    private static SubjectObjectFactSearcher INSTANCE;
    
    private static final java.util.regex.Pattern ROUND_BRACKETS     = java.util.regex.Pattern.compile("\\(.+?\\)");
    private static final java.util.regex.Pattern SQUARED_BRACKETS   = java.util.regex.Pattern.compile("\\[.+?\\]");
    private static final java.util.regex.Pattern TRASH              = java.util.regex.Pattern.compile("[^\\p{L}\\p{N}.?!' ]");
    private static final java.util.regex.Pattern WHITESPACES        = java.util.regex.Pattern.compile("\\n");

    /**
     * 
     */
    public SubjectObjectFactSearcher() {
    }
    
    /**
     * this needs to be synchronized in order to avoid calling this
     * method from every crawl thread
     * 
     * @return
     */
//    public static synchronized SubjectObjectFactSearcher getInstance() {
//        
//        if ( SubjectObjectFactSearcher.INSTANCE == null )
//            SubjectObjectFactSearcher.INSTANCE = new SubjectObjectFactSearcher();
//        
//        return SubjectObjectFactSearcher.INSTANCE;
//    }
    
    @Override
    public void generateProofs(Evidence evidence, WebSite website, DefactoModel model, Pattern pattern) {

        String websiteText  = website.getText().toLowerCase();
        
        Set<String> subjectLabels = new HashSet<String>();
        Set<String> objectLabels = new HashSet<String>();
        
        for ( String language : model.getLanguages() ) {
        	
        	subjectLabels.add(model.getSubjectLabelNoFallBack(language));
        	subjectLabels.addAll(model.getSubjectAltLabels(language));
        	
        	objectLabels.add(model.getObjectLabelNoFallBack(language));
        	objectLabels.addAll(model.getObjectAltLabels());
        }
        subjectLabels.removeAll(Collections.singleton(Constants.NO_LABEL));
        objectLabels.removeAll(Collections.singleton(Constants.NO_LABEL));

        toLowerCase(subjectLabels);
        toLowerCase(objectLabels);
        
        // combine the list to make processing a little easier
        Set<String> surfaceForms = new HashSet<String>(subjectLabels);
        surfaceForms.addAll(objectLabels);
        
        for ( String subjectLabel : subjectLabels ) { 
        	
        	// save some time
        	if ( !websiteText.contains(subjectLabel) ) continue;
        	
            for ( String objectLabel : objectLabels ) { 
            	
            	if ( !websiteText.contains(objectLabel) ) continue;
            	
            	LOGGER.debug("Search proof for: '" + subjectLabel + "' and '" + objectLabel + "'.");
            
            	if ( subjectLabel.equals(objectLabel) ) continue;
            	
                String[] subjectObjectMatches = StringUtils.substringsBetween(websiteText, " " + subjectLabel, objectLabel + " ");
                String[] objectSubjectMatches = StringUtils.substringsBetween(websiteText, " " + objectLabel, subjectLabel + " ");
                
                // we need to check for both directions
                List<String> subjectObjectOccurrences = new ArrayList<String>();
                if (subjectObjectMatches != null) for ( String s : subjectObjectMatches ) subjectObjectOccurrences.add(getShortProofPhrase(s,subjectLabel,objectLabel));
                // asdjklajsd
                List<String> objectSubjectOccurrences = new ArrayList<String>();
                if (objectSubjectMatches != null) for ( String s : objectSubjectMatches) objectSubjectOccurrences.add(getShortProofPhrase(s,objectLabel, subjectLabel));
                
                // direction: subject property object
                createProofsForEvidence(evidence, subjectObjectOccurrences, subjectLabel, objectLabel, websiteText, website, surfaceForms);
                // direction: object property subject 
                createProofsForEvidence(evidence, objectSubjectOccurrences, objectLabel, subjectLabel, websiteText, website, surfaceForms);
            }
        }
        LOGGER.debug("#sLabels: "+  subjectLabels.size() + " #oLabels:" + objectLabels.size() + " #Proofs: " + evidence.getComplexProofs().size() + " #lang: " + model.getLanguages().size());
    }

    private String getShortProofPhrase(String input, String leftLabel, String rightLabel)
    {
        String temp1="";
        String temp=input;
        do
        {
            temp1 = temp;
            temp = org.apache.commons.lang3.StringUtils.substringBetween(temp1+rightLabel, leftLabel, rightLabel);
        }while(temp!=null);
//		System.out.println("\n");
//		System.out.println("===================================");
//		System.out.println(leftLabel+temp1+rightLabel);
//		System.out.println("===================================");
//		System.out.println("\n");
        return temp1;
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
    
    
    /**
     * 
     * @param evidence
     * @param matches
     * @param patternToSearch
     * @param firstLabel
     * @param secondLabel
     * @param site
     */
    private void createProofsForEvidence(Evidence evidence, List<String> matches, String firstLabel, String secondLabel, String websiteTextLowerCase, WebSite site, Set<String> surfaceForms) {
        
        for ( String occurrence : matches ) {
            
            // it makes no sense to look at longer strings 
            if ( occurrence.split(" ").length < Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_OF_TOKENS_BETWEEN_ENTITIES") ) {
                
                String tinyContext = this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel, 25);
                
                // first we check if we can find a boa pattern inside the mathing string
//                for (Pattern boaPattern : evidence.getBoaPatterns()) { // go through all patterns and look if a non empty normalized pattern string is inside the match
                	
//                	if ( !tinyContext.contains(boaPattern.normalize()) ) continue;
                	
//                	System.out.println(boaPattern.normalize());
//                	System.out.println(tinyContext);
//                	
                	// this can only be if the patterns contains only garbage
//                	if ( boaPattern.normalize().isEmpty() ) continue;
                	
                	ComplexProof proof = new ComplexProof(evidence.getModel(), firstLabel, secondLabel, occurrence, normalizeOccurrence(tinyContext,surfaceForms), site);
                    proof.setTinyContext(this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel, 25));
                    proof.setSmallContext(this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel, 50));
                    proof.setMediumContext(this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel, 100));
                    proof.setLargeContext(this.getLeftAndRightContext(site.getText(), websiteTextLowerCase, firstLabel + occurrence + secondLabel, 150));
                    
                    evidence.addComplexProof(proof);
//                }
            }
        }
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
     * 
     * @param normalCase
     * @param lowerCase
     * @param match
     * @param contextLength
     * @return
     */
    private String getLeftAndRightContext(String normalCase, String lowerCase, String match, int contextLength) {
        
        int leftIndex = lowerCase.indexOf(match);
        int rightIndex = leftIndex + match.length();
        
        // avoid index out of bounds
        if ( leftIndex - contextLength >= 0 ) leftIndex -= contextLength;
        else leftIndex = 0;
        
        if ( rightIndex + contextLength > lowerCase.length() ) rightIndex = lowerCase.length() - 1;
        else rightIndex += contextLength;
        
        return normalCase.substring(leftIndex, rightIndex);
    }
	
	
	private void createProofsForEvidence1(Evidence evidence, HashMap<String, Integer> matches, String firstLabel, String secondLabel, WebSite site, Set<String> surfaceForms, Set<String> subjectlabels, Set<String> objectlabels) {
	        
	    for(Map.Entry<String, Integer> entry : matches.entrySet())
	    {
	    	String tinyContext = "";
	    	String sublabel ="";
		    String objlabel ="";
	    	String occurrence = entry.getKey();
	    	String OccurenecText = occurrence;
	            // it makes no sense to look at longer strings 
	            if ( occurrence.split(" ").length < Defacto.DEFACTO_CONFIG.getIntegerSetting("extract", "NUMBER_OF_TOKENS_BETWEEN_ENTITIES") ) {
	                	
	            	if(entry.getValue()>1)
	            	{
	            	Annotation doc = new Annotation(occurrence);
	    		    this.pipeline1.annotate(doc);
	    		    Map<Integer, CorefChain> corefs = doc.get(CorefChainAnnotation.class);
	    		    List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);

	    		    List<String> resolved = new ArrayList<String>();

	    		    for (CoreMap sentence : sentences) {
	    		    	//System.out.println(sentence);

	    		        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);

	    		        for (CoreLabel token : tokens) {

	    		            Integer corefClustId= token.get(CorefCoreAnnotations.CorefClusterIdAnnotation.class);
	    		            CorefChain chain = corefs.get(corefClustId);
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
	    		    //System.out.println("After mentions : " +resolvedStr);
	    		    Annotation occdoc = new Annotation(resolvedStr);
	    		    this.pipeline.annotate(occdoc);
	    		    
	    		    List<CoreMap> occsentences = occdoc.get(CoreAnnotations.SentencesAnnotation.class);
	    		    for (CoreMap sentence : occsentences) {
	    		    	String sentenceString = sentence.toString().toLowerCase();
	    		    	boolean subfound = false;
	    		    	boolean objfound = false;
	    		    	for (String string : subjectlabels) {
							if(StringUtils.containsIgnoreCase(sentenceString, string))
							{
								subfound = true;
								sublabel = string;
								break;
							}
						}
	    		    	for (String string : objectlabels) {
							if(StringUtils.containsIgnoreCase(sentenceString, string))
							{
								objfound = true;
								objlabel = string;
								break;
							}
						}
	    		    	
	    		    	if(subfound && objfound)
	    		    	{
	    		    		System.out.println("Occurence before "+occurrence);
	    		    		occurrence = sentence.toString();
	    		    		System.out.println("Occurence after "+occurrence);
	    		    		if(!(StringUtils.substringBetween(occurrence.toLowerCase(), sublabel.toLowerCase(), objlabel.toLowerCase())==null))
	    		    			tinyContext = breakString(occurrence.toLowerCase(), sublabel.toLowerCase(), objlabel.toLowerCase());
	    		    		else
	    		    			tinyContext = breakString(occurrence.toLowerCase(), objlabel.toLowerCase(), sublabel.toLowerCase());
	    		    		break;
	    		    	}
	    		    	
	    		    	else if ((subfound || objfound) && sentence.toString().contains(site.getPredicate()))
	    		    	{
	    		    		occurrence = sentence.toString();
	    		    		/*if(!(StringUtils.substringBetween(occurrence.toLowerCase(), sublabel.toLowerCase(), objlabel.toLowerCase())==null))
	    		    			tinyContext = breakString(occurrence.toLowerCase(), sublabel.toLowerCase(), objlabel.toLowerCase());
	    		    		else
	    		    			tinyContext = breakString(occurrence.toLowerCase(), objlabel.toLowerCase(), sublabel.toLowerCase());*/
	    		    		break;
	    		    	}
	    		    	
	    		    }
	            	}
	            	
	            	if(OccurenecText.equals(occurrence))
	            	{
	            		boolean subfound = false;
	    		    	boolean objfound = false;	    		    	
	            		for (String string : subjectlabels) {
							if(StringUtils.containsIgnoreCase(occurrence, string))
							{
								subfound = true;
								sublabel = string;
								break;
							}
						}
	    		    	for (String string : objectlabels) {
							if(StringUtils.containsIgnoreCase(occurrence, string))
							{
								objfound = true;
								objlabel = string;
								break;
							}
						}
	    		    	
	    		    	if(subfound && objfound)
	    		    	{
	    		    		if(!(StringUtils.substringBetween(occurrence.toLowerCase(), sublabel.toLowerCase(), objlabel.toLowerCase())==null))
	    		    			tinyContext = StringUtils.substringBetween(occurrence.toLowerCase(), sublabel.toLowerCase(), objlabel.toLowerCase());
	    		    		else
	    		    			tinyContext = StringUtils.substringBetween(occurrence.toLowerCase(), objlabel.toLowerCase(), sublabel.toLowerCase());
	    		    	}
	            		
	            	}
	                	
	                	ComplexProof proof = new ComplexProof(evidence.getModel(), sublabel, objlabel, occurrence, normalizeOccurrence(occurrence,surfaceForms), site);
	                	proof.setTinyContext(tinyContext);
	                	//System.out.println(proof.getTinyContext());
	//                    System.out.println("Found Proof in website "+site.getUrl());
	//                    System.out.println(occurrence);
	                    
	                    evidence.addComplexProof(proof);
	//                }
	            }
	        }
	    }
		
		
		    private String breakString(String input, String leftLabel, String rightLabel){
					String temp1="";
					String temp=input;
					do
					{
						temp1 = temp;
						temp = org.apache.commons.lang3.StringUtils.substringBetween(temp1+rightLabel, leftLabel, rightLabel);
					}while(temp!=null);
			//		System.out.println("\n");
			//		System.out.println("===================================");
			//		System.out.println(leftLabel+temp1+rightLabel);
			//		System.out.println("===================================");
			//		System.out.println("\n");
					return temp1;
			}
			
				public void generateProofs1(Evidence evidence, WebSite website, DefactoModel model, Pattern pattern) {
	
			//	    String websiteText  = website.getText().toLowerCase();
					String url = website.getUrl();
					System.out.println(website.getUrl());
					this.pipeline = model.pipeline;
					this.pipeline1 = model.pipeline1;
					Set<String> subjectLabels = new HashSet<String>();
					Set<String> objectLabels = new HashSet<String>();
					/****** Get the surface forms for Subject and Object *********/
					for ( String language : model.getLanguages() ) {
						subjectLabels.add(model.getSubjectLabelNoFallBack(language));
						subjectLabels.addAll(model.getSubjectAltLabels(language));
						
						objectLabels.add(model.getObjectLabelNoFallBack(language));
						objectLabels.addAll(model.getObjectAltLabels(language));
					}
					subjectLabels.removeAll(Collections.singleton(Constants.NO_LABEL));
					objectLabels.removeAll(Collections.singleton(Constants.NO_LABEL));
					/**** Remove the old Logic ****/
			/*	    toLowerCase(subjectLabels);
					toLowerCase(objectLabels);
					String[] subject  = new String[subjectLabels.size()];
					String[] normalized  = new String[subjectLabels.size()];
					subjectLabels.toArray(subject);
					for (int i=0 ; i<subjectLabels.size();i++) {
						normalized[i] = "subjectFound";
					}
					
					String[] object  = new String[subjectLabels.size()];
					String[] objnormalized  = new String[subjectLabels.size()];
					objectLabels.toArray(object);
					for (int i=0 ; i<objectLabels.size();i++) {
						objnormalized[i] = "objectFound";
					}
					
					String subjectnormwebsite = StringUtils.replaceEach(websiteText, subject, normalized);
					String objectnormwebsite = StringUtils.replaceEach(subjectnormwebsite, object, objnormalized);*/
					
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
					String actualText = website.getText();
					for (java.util.regex.Pattern pattern2 : subpatterns) {
						actualText = pattern2.matcher(actualText).replaceAll("subjectFound");
					}
					for (java.util.regex.Pattern pattern2 : objpatterns) {
						actualText = pattern2.matcher(actualText).replaceAll("objectFound");
					}
					
					/**** Annotate the website text and normalized website text using SNLP sentence split "ssplit" ****/
					Annotation doc = new Annotation(actualText);
					Annotation docNormal = new Annotation(website.getText());
					this.pipeline.annotate(doc);
					this.pipeline.annotate(docNormal);
					HashMap<String, Integer> subOjectPhrases = findProofPhrase(doc, docNormal, "subjectFound", "objectFound");
					HashMap<String, Integer> objSubjectPhrases = findProofPhrase(doc, docNormal, "objectFound", "subjectFound");
					HashMap<String, Integer> proofPhrases = new HashMap<>();
					proofPhrases.putAll(subOjectPhrases);
					proofPhrases.putAll(objSubjectPhrases);
					Set<String> surfaceForms = new HashSet<String>(subjectLabels);
					surfaceForms.addAll(objectLabels);
					createProofsForEvidence1(evidence, proofPhrases, "Albert Einstein", "Nobel Prize in Physics", website, surfaceForms, subjectLabels, objectLabels);
					// combine the list to make processing a little easier
						
						// save some time
					/*	if ( objectnormwebsite.contains("subjectFound") && objectnormwebsite.contains("objectFound"))
							
						{
							
							String[] subjectObjectMatchesnor = StringUtils.substringsBetween(objectnormwebsite, " subjectFound", "objectFound ");
							String[] objectSubjectMatchesnor = StringUtils.substringsBetween(objectnormwebsite, " objectFound", "subjectFound ");
							// we need to check for both directions
							List<String> subjectObjectOccurrences = new ArrayList<String>();
							if (subjectObjectMatchesnor != null) for ( String s : subjectObjectMatchesnor ) subjectObjectOccurrences.add(breakString(s,"subjectFound","objectFound"));
							// asdjklajsd
							List<String> objectSubjectOccurrences = new ArrayList<String>();
							if (objectSubjectMatchesnor != null) for ( String s : objectSubjectMatchesnor) objectSubjectOccurrences.add(breakString(s, "objectFound", "subjectFound"));
							
							// direction: subject property object
							createProofsForEvidence(evidence, subjectObjectOccurrences, "subjectFound", "objectFound", objectnormwebsite, website, surfaceForms);
							// direction: object property subject 
							createProofsForEvidence(evidence, objectSubjectOccurrences, "objectFound", "subjectFound", objectnormwebsite, website, surfaceForms);
						}
					LOGGER.debug("#sLabels: "+  subjectLabels.size() + " #oLabels:" + objectLabels.size() + " #Proofs: " + evidence.getComplexProofs().size() + " #lang: " + model.getLanguages().size());*/
				}
				
				private HashMap<String, Integer> findProofPhrase(Annotation doc, Annotation docNormal, String labelFirst, String labelSecond)
				{
					int sentenceCount = 0;
					int count = 0;
					int subjoccur = 0;
					boolean subjectFound = false;
					boolean objectFound = false;
					TreeMap<Integer, Integer> sent = new TreeMap<>();
					List<CoreMap> sentences = doc.get(CoreAnnotations.SentencesAnnotation.class);
					List<CoreMap> sentencesNormal = docNormal.get(CoreAnnotations.SentencesAnnotation.class);
					for (CoreMap sentence : sentences) {
						if(sentence.toString().contains(labelFirst) && sentence.toString().contains(labelSecond))
						{
							subjectFound = false;
							sent.put(count, count);
							count++;
							continue;
						}
						if(sentence.toString().contains(labelFirst))
						{
							subjectFound = true;
							sentenceCount = 1;
							subjoccur = count;
							count++;
							continue;
						}
						
						if(subjectFound && !(sentence.toString().contains(labelSecond)))
						{
							sentenceCount++;
							count++;
							continue;
						}
						
						if(sentence.toString().contains(labelSecond) && subjectFound)
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
					
					HashMap<String, Integer> subjectObjectStrNormal = new HashMap<String, Integer>();
					for(Entry<Integer, Integer> entry : sent.entrySet()) {
						  if(entry.getKey().equals(entry.getValue()))
						  {
							  subjectObjectStrNormal.put(sentencesNormal.get(entry.getKey()).toString(),1);
							  continue;
						  }
						  int k=0;
						  String temp = "";
						  for(int i = entry.getKey(); i<=entry.getValue();i++)
						  {
							  temp = temp+" "+sentencesNormal.get(i).toString();
							  k++;
						  }
						  subjectObjectStrNormal.put(temp.trim(), k);
						}
					return subjectObjectStrNormal;
				}
}
