package rdf;

import org.apache.commons.io.IOUtils;
import org.apache.jena.rdf.model.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author DANISH AHMED on 3/22/2018
 * Extraction idea taken from: https://github.com/SmartDataAnalytics/DeFacto
 * and then modified/optimized according to our need
 */
public class TripleExtractor {

    private Model model;     // provides model after file has been read

    public String getSubject() {
        return subject.label;
    }

    public String getObject() {
        return object.label;
    }
    private RDFResource subject;
    private Property predicate;
    private RDFResource object;

    private String subjectUri;

    public String getPredicateUri() {
        return predicateUri;
    }

    private String predicateUri;
    private String objectUri;

    private Model simplifiedModel;
    private String simplifiedData;

    public TripleExtractor(String file, boolean readFromFile) throws IOException {
        Model model = ModelFactory.createDefaultModel();
        if (readFromFile)
            model.read(new FileInputStream(file), null, "TTL");
        else
            model.read(IOUtils.toInputStream(file, "UTF-8"), null, "TTL");

        setModel(model);

        parseStatements();
        setUris();

        setSimplifiedData();
    }

    private void setUris() {
        subjectUri = getResourceUri(subject);
        objectUri = getResourceUri(object);
        predicateUri = predicate.getURI();
    }

    private void setModel(Model model) {
        this.model = model;
    }

    private void setSimplifiedData() {

        simplifiedData = String.format("<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://www.w3.org/1999/02/22-rdf-syntax-ns#Statement> .\n" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#subject> <%s> .\n" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#predicate> <%s> .\n" +
                "<http://swc2017.aksw.org/task/dataset/s> <http://www.w3.org/1999/02/22-rdf-syntax-ns#object> "
                , subjectUri
                , predicateUri);

        if (object.resource.isResource())
            simplifiedData = simplifiedData + String.format("<%s> .\n", objectUri);
        else if (object.resource.isLiteral())
            simplifiedData = simplifiedData + String.format("\"%s\" .\n", objectUri);

        String labels = String.format("<%s> <http://www.w3.org/2000/01/rdf-schema#label> \"%s\"@en .\n" +
                "<%s> <http://www.w3.org/2000/01/rdf-schema#label> \"%s\"@en .\n"
                , subjectUri, subject.label
                , objectUri, object.label);
        simplifiedData = simplifiedData + labels;
    }

    public Model getSimplifiedModel() {
        return this.simplifiedModel;
    }

    public String getSimplifiedData() {
        return this.simplifiedData;
    }

    private void setSimplifiedModel() {
        simplifiedModel = ModelFactory.createDefaultModel();
        simplifiedModel.read(new ByteArrayInputStream(simplifiedData.getBytes()), null, "TTL");
    }

    private void parseStatements() {
        boolean startNode = false;

        StmtIterator stmtIterator = this.model.listStatements();
        Resource subjectNode = null;
        RDFNode objectNode = null;
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();

            // look for starting node
            if (statement.getSubject().getURI().matches("^.*__[0-9]*$")) {
                startNode = true;
                if (statement.getObject().isResource()) {
                    subjectNode = statement.getSubject();
                    if (subjectNode == null)
                        continue;
                    processTriple(statement, subjectNode, objectNode);
                }
            }
        }

        if (!startNode) {
            stmtIterator = this.model.listStatements();
            subjectNode = null;
            objectNode = null;

            while (stmtIterator.hasNext()) {
                Statement statement = stmtIterator.next();

                if (statement.getSubject() != null) {
                    if (statement.getObject().isResource()) {
                        subjectNode = statement.getSubject();
                        if (subjectNode == null)
                            continue;
                        processTriple(statement, subjectNode, objectNode);
                    }
                }
            }
        }
    }

    private void processTriple(Statement statement, Resource subjectNode, RDFNode objectNode) {
        this.predicate = statement.getPredicate();
        objectNode = statement.getObject();

        // check if object is resource and has edges, parse until you get Literal
        getObject(statement, objectNode);

        // now find if current statement subject node is part of object node
        // then make that node as subject
        getSubject(subjectNode);
    }

    /**
     * retrieve subject node
     * @param subjectNode subject initial node
     */
    private void getSubject(Resource subjectNode) {
        this.subject = new RDFResource(subjectNode.asResource(), model);
        StmtIterator stmtIterator = this.model.listStatements();
        while (stmtIterator.hasNext()) {
            Statement statement = stmtIterator.next();

            if (statement.getObject().isResource()
                    && statement.getObject().asResource().getURI().equals(subjectNode.getURI())) {
                subjectNode = statement.getSubject();
                this.subject = new RDFResource(subjectNode.asResource(), model);
            }
        }
    }

    /**
     * retrieve object node
     * @param statement node statement
     */
    private RDFNode getObject(Statement statement, RDFNode objectNode) {
        if (objectNode.isLiteral()) {
            this.object = new RDFResource(statement.getSubject().asResource(), this.model);
            return objectNode;
        }

        if (objectNode.isResource()) {
            // parse all statements again
            // if object URI becomes subject URI, it will either have literal or resource
            // if it's a literal, return object
            // else call this function again

            StmtIterator stmtIterator = this.model.listStatements();
            while (stmtIterator.hasNext()) {
                Statement stmt = stmtIterator.next();

                if (stmt.getSubject().getURI().equals(objectNode.asResource().getURI())) {
                    RDFNode objNode = stmt.getObject();
                    this.object = new RDFResource(stmt.getSubject().asResource(), this.model);

                    return getObject(stmt, objNode);
                }
            }
        }
        return null;
    }

    private String getResourceUri(RDFResource resource) {
        if (!resource.owlSameAsList.isEmpty())
            return RDFResource.getDBpediaUri(resource);
        return resource.uri;
    }

    public static void main(String[] args) throws IOException {
        String data = "" +
                "@prefix fbase: <http://rdf.freebase.com/ns> .\n" +
                "@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
                "@prefix dbo:   <http://dbpedia.org/ontology/> .\n" +
                "@prefix owl:   <http://www.w3.org/2002/07/owl#> .\n" +
                "@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
                "@prefix skos:  <http://www.w3.org/2004/02/skos/core#> .\n" +
                "\n" +
                "<http://rdf.freebase.com/ns/m.0jcx__24>\n" +
                "        dbo:award  <http://rdf.freebase.com/ns/m.0dt39> ;\n" +
                "        dbo:from   \"1921\"^^xsd:gYear ;\n" +
                "        dbo:to     \"1921\"^^xsd:gYear .\n" +
                "\n" +
                "<http://rdf.freebase.com/ns/m.0dt39>\n" +
                "        rdfs:label     \"Nobel Prize in Physics\"@en , \"Prix Nobel de physique\"@fr , \"Nobelpreis für Physik\"@de ;\n" +
                "        owl:sameAs     <http://fr.dbpedia.org/resource/Prix_Nobel_de_physique> , <http://fr.dbpedia.org/resource/> , <http://de.dbpedia.org/resource/Nobelpreis_für_Physik> , <http://dbpedia.org/resource/Nobel_Prize_in_Physics> , <http://de.dbpedia.org/resource/Liste_der_Nobelpreisträger_für_Physik> , <http://dbpedia.org/resource/List_of_Nobel_laureates_in_Physics> ;\n" +
                "        skos:altLabel  \"Nobel Physics Prize\"@en , \"Nobel laureates in physics\"@fr , \"Nobel laureates in physics\"@en , \"Nobel laureates in physics\"@de , \"Physik-Nobelpreis\"@de , \"Nobel prize in physics\"@fr , \"Nobel prize in physics\"@en , \"Nobel prize in physics\"@de , \"Nobel Prize for physics\"@fr , \"Nobel Prize for physics\"@en , \"Nobel Prize for physics\"@de , \"Prix Nobel de Physique\"@fr , \"Physics\"@fr , \"Physics\"@en , \"Physics\"@de , \"Nobel Prizes for Physics\"@fr , \"Nobel Prizes for Physics\"@en , \"Nobel Prizes for Physics\"@de , \"Liste der Physiknobelpreistrager\"@de , \"Liste des Prix Nobel de physique\"@fr , \"Liste der Nobelpreisträger für Physik\"@de , \"Nobelpreis fur Physik\"@de , \"Nobel prize for Physics\"@fr , \"Nobel prize for Physics\"@de , \"Nobel prize for Physics\"@en , \"Physiknobelpreis\"@de , \"Nobel Prize in Physics\"@fr , \"Nobel Prize in Physics\"@de , \"Nobel Prize in Physics\"@en , \"Prix nobel de physique\"@fr , \"PhysicS\"@fr , \"PhysicS\"@en , \"PhysicS\"@de , \"Nobel Prize For Physics - 2007\"@fr , \"Liste der Nobelpreistrager fur Physik\"@de , \"Nobel Prize For Physics - 2007\"@en , \"Nobel Prize For Physics - 2007\"@de , \"Prix Nobel de physique\"@fr , \"Physics nobel prize\"@fr , \"Physics nobel prize\"@en , \"Physics nobel prize\"@de , \"Noble prize in physics\"@fr , \"Noble prize in physics\"@en , \"Noble prize in physics\"@de , \"Liste der Physiknobelpreisträger\"@de , \"Nobel Prize for Physics\"@fr , \"Nobel prize physics\"@fr , \"Nobel Prize for Physics\"@de , \"Nobel Prize for Physics\"@en , \"Nobel prize physics\"@de , \"Nobel prize physics\"@en , \"Nobel Prize in physics\"@fr , \"Nobel Prize in physics\"@en , \"Nobel Prize in physics\"@de , \"Nobelpreis für Physik\"@de , \"Nobel Physics Prize\"@fr , \"Nobel Physics Prize\"@de .\n" +
                "\n" +
                "<http://rdf.freebase.com/ns/m.0jcx>\n" +
                "        rdfs:label         \"Albert Einstein\"@fr , \"Albert Einstein\"@en , \"Albert Einstein\"@de ;\n" +
                "        dbo:recievedAward  <http://rdf.freebase.com/ns/m.0jcx__24> ;\n" +
                "        owl:sameAs         <http://dbpedia.org/resource/Albert_Einstein> , <http://fr.dbpedia.org/resource/Albert_Einstein> , <http://de.dbpedia.org/resource/Albert_Einstein> ;\n" +
                "        skos:altLabel      \"Albert\"@fr , \"Compassionate Zionism and Albert Einstein\"@fr , \"Compassionate Zionism and Albert Einstein\"@de , \"Compassionate Zionism and Albert Einstein\"@en , \"A. Einstein\"@fr , \"Albert Einstin\"@fr , \"A. Einstein\"@de , \"A. Einstein\"@en , \"Albert Einstin\"@en , \"Albert Einstin\"@de , \"Albert Einstein's\"@fr , \"Albert Einstein's\"@de , \"Albert Einstein's\"@en , \"Einstein, Albert\"@fr , \"Einstein, Albert\"@de , \"Einstein, Albert\"@en , \"Einsteinian\"@fr , \"Einsteinian\"@de , \"Einsteinian\"@en , \"A Tribute to Einstein\"@fr , \"A Tribute to Einstein\"@de , \"A Tribute to Einstein\"@en , \"Albert Enstien\"@fr , \"Albert Enstien\"@de , \"Albert Enstien\"@en , \"Albert Enstin\"@fr , \"Albert Enstin\"@de , \"Albert Enstin\"@en , \"Ejnstejno\"@fr , \"Ejnstejno\"@de , \"Ejnstejno\"@en , \"Einstein\"@fr , \"Einstein\"@de , \"Einstein\"@en , \"Al Einstein\"@fr , \"Albert LaFache Einstein\"@fr , \"Albert LaFache Einstein\"@en , \"Albert LaFache Einstein\"@de , \"Al Einstein\"@de , \"Al Einstein\"@en , \"Einstein on socialism\"@fr , \"Einstein on socialism\"@de , \"Einstein on socialism\"@en , \"Albert Einstien\"@fr , \"Albert Einstien\"@en , \"Albert Einstien\"@de , \"Einstein's theory\"@fr , \"Einstein's theory\"@de , \"Einstein's theory\"@en , \"Albert Enstein\"@fr , \"Albert Enstein\"@de , \"Albert Enstein\"@en , \"Einstien\"@fr , \"Einstien\"@de , \"Einstien\"@en , \"Alber Enstien\"@fr , \"Alber Enstien\"@de , \"Alber Enstien\"@en , \"Albert Einstein\"@fr , \"Einsetein\"@fr , \"Albert Einstein\"@de , \"Einsetein\"@en , \"Einsetein\"@de , \"Albert Einstein\"@en , \"Einstien, Albert\"@fr , \"Albert einstein\"@fr , \"Albert einstein\"@en , \"Albert einstein\"@de , \"Einstien, Albert\"@en , \"Einstien, Albert\"@de , \"God does not play dice\"@fr , \"Alber Einstein\"@fr , \"God does not play dice\"@de , \"Alber Einstein\"@en , \"Alber Einstein\"@de , \"God does not play dice\"@en , \"Albert Eienstein\"@fr , \"Albert Eienstein\"@de , \"Albert Eienstein\"@en .";

        TripleExtractor tripleExtractor = new TripleExtractor(data, false);
//        TripleExtractor tripleExtractor = new TripleExtractor("Einstein.ttl", true);

        System.out.println(tripleExtractor.simplifiedData);
    }
}
