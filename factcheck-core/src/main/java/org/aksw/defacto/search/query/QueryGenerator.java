package org.aksw.defacto.search.query;

import java.io.IOException;
import java.util.*;

import org.aksw.defacto.Constants;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.boa.RelativePredicates;
import org.aksw.defacto.model.DefactoModel;
/*import org.aksw.simba.bengal.verbalizer.SemWeb2NLVerbalizer;*/
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.dllearner.kb.sparql.SparqlEndpoint;
/*import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.bengal.paraphrasing.Paraphrasing;*/
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.jena.rdf.model.Statement;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * 
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 *
 */
public class QueryGenerator {

    public static final BoaPatternSearcher patternSearcher = new BoaPatternSearcher();
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryGenerator.class);
    private DefactoModel model;

/*    protected SemWeb2NLVerbalizer verbalizer =
            new SemWeb2NLVerbalizer(SparqlEndpoint.getEndpointDBpedia(), true, true);*/
    
    /**
     * 
     * @param model
     */
    public QueryGenerator(DefactoModel model) {
        
        this.model = model;
    }
    
    /**
     * 
     * @return
     */
    public Map<Pattern,MetaQuery> getSearchEngineQueries(String language){
        
        // and generate the query strings 
        return this.generateSearchQueries(model.getFact(), language);
    }
    
    /**
     * 
     * @param fact
     * @param language
     * @return
     */
    private Map<Pattern,MetaQuery> generateSearchQueries(Statement fact, String language){
     
        Map<Pattern,MetaQuery> queryStrings =  new HashMap<Pattern,MetaQuery>();
        String subjectLabel = model.getSubjectLabelNoFallBack(language);//.replaceAll("\\(.+?\\)", "").trim(); 
        String objectLabel  = model.getObjectLabelNoFallBack(language);//.replaceAll("\\(.+?\\)", "").trim();
        
        // we dont have labels in the given language so we generate a foreign query with english labels
        if ( subjectLabel.equals(Constants.NO_LABEL) || objectLabel.equals(Constants.NO_LABEL) ) {
        	
        	subjectLabel = model.getSubjectLabel("en");
        	objectLabel = model.getObjectLabel("en");
        }

        List<Statement> statements = new ArrayList<>();

        statements.add(fact);

        RelativePredicates relativePredicates = new RelativePredicates();

        LOGGER.info("get relative predicates for : "+fact.getPredicate().getLocalName());
        LOGGER.info("relatives predicates size : "+relativePredicates.all(fact.getPredicate().getLocalName()).size());
        for(String p:relativePredicates.all(fact.getPredicate().getLocalName())){
            Pattern pattern = new Pattern("?R? "+p+" ?D?",language);
            pattern.naturalLanguageRepresentationWithoutVariables = p;
            pattern.normalize();

            MetaQuery metaQueryq = new MetaQuery(subjectLabel, p, objectLabel, language, null);
            System.out.println(metaQueryq);
            queryStrings.put(pattern, metaQueryq);
        }



        // TODO
        // query boa index and generate the meta queries
/*       for (Pattern pattern : patternSearcher.getNaturalLanguageRepresentations1(fact.getPredicate().getURI(), language)) {
        	
        	if ( !pattern.getNormalized().trim().isEmpty() ) {
        		
        		MetaQuery metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null);
        		System.out.println(metaQuery);
        		queryStrings.put(pattern, metaQuery);
        	}
        }*/
       
        return queryStrings;
    }
}
