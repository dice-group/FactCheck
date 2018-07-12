package org.aksw.defacto.search.query;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aksw.defacto.Constants;
import org.aksw.defacto.boa.BoaPatternSearcher;
import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.model.DefactoModel;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.ParseException;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.rdf.model.Statement;

/**
 * @author Daniel Gerber <dgerber@informatik.uni-leipzig.de>
 */
public class QueryGenerator {

    public static final BoaPatternSearcher patternSearcher = new BoaPatternSearcher();
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryGenerator.class);
    private DefactoModel model;
    //private RestClient restClientobj;

    /**
     * @param model
     */
    public QueryGenerator(DefactoModel model) {

        this.model = model;
        //  restClientobj = RestClient.builder(new HttpHost("131.234.28.255" , 6060, "http")).build();
    }

    /**
     * @return
     */
    public Map<Pattern, MetaQuery> getSearchEngineQueries(String language) {

        // and generate the query strings
        return this.generateSearchQueries(model.getFact(), language);
    }

    /**
     * @param fact
     * @param language
     * @return
     */
    private Map<Pattern, MetaQuery> generateSearchQueries(Statement fact, String language) {

        Map<Pattern, MetaQuery> queryStrings = new HashMap<Pattern, MetaQuery>();
        String subjectLabel = model.getSubjectLabelNoFallBack(language);//.replaceAll("\\(.+?\\)", "").trim();
        String objectLabel = model.getObjectLabelNoFallBack(language);//.replaceAll("\\(.+?\\)", "").trim();

        // we dont have labels in the given language so we generate a foreign query with english labels
        if (subjectLabel.equals(Constants.NO_LABEL) || objectLabel.equals(Constants.NO_LABEL)) {

            subjectLabel = model.getSubjectLabel("en");
            objectLabel = model.getObjectLabel("en");
        }

        // TODO
        // query boa index and generate the meta queries
        LOGGER.info("Using predicate {}", fact.getPredicate());
        //  System.out.println();
        for (Pattern pattern : patternSearcher.getNaturalLanguageRepresentations(fact.getPredicate().getURI(), language)) {

            if (!pattern.getNormalized().trim().isEmpty()) {

                MetaQuery metaQuery = new MetaQuery(subjectLabel, pattern.getNormalized(), objectLabel, language, null);
                LOGGER.info("Generating Meta Query \"{}\"", metaQuery);
                queryStrings.put(pattern, metaQuery);
            }
        }

        return queryStrings;
    }
}
