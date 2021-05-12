package org.aksw.defacto.search.query;

import org.aksw.defacto.boa.Pattern;
import org.aksw.defacto.boa.RelativePredicates;
import org.aksw.gerbil.transfer.nif.Document;
import org.aksw.simba.bengal.paraphrasing.Paraphrasing;
import org.aksw.simba.bengal.verbalizer.SemWeb2NLVerbalizer;
import org.apache.jena.rdf.model.Statement;
import org.dllearner.kb.sparql.SparqlEndpoint;

import java.util.ArrayList;
import java.util.List;

public class PaternGenerator {


    public List<Pattern> generate(Statement fact,String language){

        List<Pattern> returnVal = new ArrayList<>();

        List<Statement> statements = new ArrayList<>();

        statements.add(fact);

        RelativePredicates relativePredicates = new RelativePredicates();

        for(String p :relativePredicates.all(fact.getPredicate().getLocalName())) {
            Pattern pattern = new Pattern("?R? " + p + " ?D?", language);
            pattern.naturalLanguageRepresentationWithoutVariables = p;
            pattern.normalize();
            returnVal.add(pattern);
        }
        return returnVal;
    }
}
